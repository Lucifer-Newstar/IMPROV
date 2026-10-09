package wo.ap;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The core loop: today's quest board, tap-to-log, and XP awarding.
 *
 * Quest generation is lazy (locked decision D3): the first read of a new
 * quest_date generates the board from the templates the user's level unlocks,
 * snapshotting rewards onto the rows (D7). Awarding is exactly-once via the
 * ledger's unique idempotency key, applies the rank multiplier and the daily
 * XP soft cap, and refreshes the derived progression state in the same
 * transaction.
 */
@Service
public class QuestService {

    private final UserQuestRepository userQuestRepository;
    private final QuestTemplateRepository questTemplateRepository;
    private final UserStatsRepository userStatsRepository;
    private final DaySummaryRepository daySummaryRepository;
    private final XpLedgerRepository xpLedgerRepository;
    private final RankTierRepository rankTierRepository;
    private final ProgressionService progressionService;
    private final Clock clock;
    private final int maxQuestsPerDay;
    private final long dailySoftCap;
    private final MeterRegistry meterRegistry;

    public QuestService(
            UserQuestRepository userQuestRepository,
            QuestTemplateRepository questTemplateRepository,
            UserStatsRepository userStatsRepository,
            DaySummaryRepository daySummaryRepository,
            XpLedgerRepository xpLedgerRepository,
            RankTierRepository rankTierRepository,
            ProgressionService progressionService,
            Clock clock,
            @Value("${improv.quests.max-quests-per-day}") int maxQuestsPerDay,
            @Value("${improv.quests.daily-soft-cap}") long dailySoftCap,
            MeterRegistry meterRegistry) {
        this.userQuestRepository = userQuestRepository;
        this.questTemplateRepository = questTemplateRepository;
        this.userStatsRepository = userStatsRepository;
        this.daySummaryRepository = daySummaryRepository;
        this.xpLedgerRepository = xpLedgerRepository;
        this.rankTierRepository = rankTierRepository;
        this.progressionService = progressionService;
        this.clock = clock;
        this.maxQuestsPerDay = maxQuestsPerDay;
        this.dailySoftCap = dailySoftCap;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public QuestBoardResponse todayBoard(User user) {
        LocalDate today = LocalDate.now(clock.withZone(StreakService.zoneFor(user)));
        List<UserQuest> quests = userQuestRepository.findByUserIdAndQuestDateOrderByIdAsc(user.getId(), today);
        if (quests.isEmpty()) {
            quests = generateQuests(user, today);
        }
        Instant resetAt = today.plusDays(1).atStartOfDay(StreakService.zoneFor(user)).toInstant();
        return new QuestBoardResponse(today, resetAt, quests.stream().map(QuestService::toDto).toList());
    }

    /** Incremental logging (tap-to-log 5 / 10 / 20, or an exact entry). */
    @Transactional
    public UserQuestDto log(User user, long questId, long amount) {
        UserQuest quest = userQuestRepository.findByIdAndUserId(questId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Quest not found"));
        if (quest.isCompleted()) {
            return toDto(quest); // idempotent: logging a completed quest changes nothing
        }
        long cap = maxLogPerCall(quest.getType());
        if (amount > cap) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Amount too large for this quest (max " + cap + " per log)");
        }
        quest.setLogged(Math.min(quest.getTarget(), quest.getLogged() + amount));
        if (quest.getLogged() >= quest.getTarget()) {
            award(user, quest);
        }
        return toDto(userQuestRepository.save(quest));
    }

    /** One-shot completion for WORKOUT / CUSTOM quests (and any quest, really). */
    @Transactional
    public UserQuestDto complete(User user, long questId) {
        UserQuest quest = userQuestRepository.findByIdAndUserId(questId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Quest not found"));
        if (!quest.isCompleted()) {
            quest.setLogged(quest.getTarget());
            award(user, quest);
        }
        return toDto(quest);
    }

    // ------------------------------------------------------------------
    // Quest generation (lazy, generate-on-read — D3)
    // ------------------------------------------------------------------

    private List<UserQuest> generateQuests(User user, LocalDate date) {
        int level = userStatsRepository.findByUserId(user.getId())
                .map(UserStats::getLevel)
                .orElse(1);
        List<QuestTemplate> eligible = questTemplateRepository
                .findByMinLevelLessThanEqualOrderByMinLevelAscIdAsc(level);
        List<UserQuest> quests = new ArrayList<>();
        for (QuestTemplate template : rotate(eligible, date)) {
            quests.add(userQuestRepository.save(UserQuest.builder()
                    .user(user)
                    .questDate(date)
                    .template(template)
                    .name(template.getName())
                    .description(template.getDescription())
                    .type(template.getType())
                    .target(template.getTarget())
                    .unit(template.getUnit())
                    .statCode(template.getStatCode())
                    .statAmount(template.getStatAmount())
                    .xpReward(template.getXpReward())
                    .build()));
        }
        return quests;
    }

    /**
     * Deterministic daily rotation: once more templates are eligible than
     * there are daily slots, the starting point rotates by day, so the board
     * is not the same five quests every day (template fatigue).
     */
    private List<QuestTemplate> rotate(List<QuestTemplate> eligible, LocalDate date) {
        if (eligible.size() <= maxQuestsPerDay) {
            return eligible;
        }
        int offset = (int) (date.toEpochDay() % eligible.size());
        List<QuestTemplate> selected = new ArrayList<>();
        for (int i = 0; i < maxQuestsPerDay; i++) {
            selected.add(eligible.get((offset + i) % eligible.size()));
        }
        return selected;
    }

    // ------------------------------------------------------------------
    // XP awarding
    // ------------------------------------------------------------------

    /**
     * Awards a completed quest: rank multiplier, daily soft cap, ledger row,
     * day summary upsert, derived-state refresh — all in one transaction.
     * Exactly-once via the ledger's unique idempotency key.
     */
    private void award(User user, UserQuest quest) {
        String key = "quest:" + quest.getId();
        if (xpLedgerRepository.existsByIdempotencyKey(key)) {
            quest.setCompleted(true);
            return;
        }
        UserStats stats = progressionService.statsFor(user);
        double multiplier = rankTierRepository.findByRank(stats.getRank())
                .map(RankTier::getXpMultiplier)
                .orElse(1.0);
        long awarded = Math.round(quest.getXpReward() * multiplier);
        long todayXp = daySummaryRepository
                .findByUserIdAndQuestDate(user.getId(), quest.getQuestDate())
                .map(DaySummary::getXpEarned)
                .orElse(0L);
        if (todayXp >= dailySoftCap) {
            awarded = Math.round(awarded * 0.5); // soft cap: half XP past the daily cap
        }

        xpLedgerRepository.save(XpLedger.builder()
                .user(user)
                .amount(awarded)
                .source(XpSource.QUEST)
                .quest(quest)
                .questDate(quest.getQuestDate())
                .idempotencyKey(key)
                .build());

        DaySummary day = daySummaryRepository
                .findByUserIdAndQuestDate(user.getId(), quest.getQuestDate())
                .orElseGet(() -> DaySummary.builder().user(user).questDate(quest.getQuestDate()).build());
        day.setXpEarned(day.getXpEarned() + awarded);
        day.setQuestsCompleted(day.getQuestsCompleted() + 1);
        daySummaryRepository.save(day);

        stats.setTotalXp(stats.getTotalXp() + awarded);
        progressionService.applyDerived(user, stats);

        quest.setCompleted(true);
        quest.setAwardedXp(awarded);
        quest.setCompletedAt(Instant.now(clock));
        meterRegistry.counter("improv.quests.completed").increment();
        meterRegistry.counter("improv.xp.awarded").increment(awarded);
    }

    /** Plausibility bounds per quest type (anti-abuse, mechanics §6.5). */
    private static long maxLogPerCall(QuestType type) {
        return switch (type) {
            case REPS, SETS -> 500;
            case DURATION -> 3600;
            case WORKOUT, CUSTOM -> 500;
        };
    }

    private static UserQuestDto toDto(UserQuest quest) {
        return new UserQuestDto(
                quest.getId(),
                quest.getTemplate().getId(),
                quest.getName(),
                quest.getDescription(),
                quest.getType().name(),
                quest.getTarget(),
                quest.getUnit(),
                quest.getStatCode(),
                quest.getStatAmount(),
                quest.getXpReward(),
                quest.getAwardedXp(),
                quest.getLogged(),
                quest.isCompleted());
    }
}
