package wo.ap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Level and rank derivation plus the cached progression state
 * (locked decisions D5/D6). Level comes from walking the level_curve config
 * table; rank comes from the ranks config table; streaks come from
 * {@link StreakService}. Everything is recomputable from the XP ledger.
 */
@Service
public class ProgressionService {

    private final UserStatsRepository userStatsRepository;
    private final DaySummaryRepository daySummaryRepository;
    private final LevelCurveRepository levelCurveRepository;
    private final RankTierRepository rankTierRepository;
    private final StreakService streakService;
    private final Clock clock;
    private final long dailySoftCap;

    public ProgressionService(
            UserStatsRepository userStatsRepository,
            DaySummaryRepository daySummaryRepository,
            LevelCurveRepository levelCurveRepository,
            RankTierRepository rankTierRepository,
            StreakService streakService,
            Clock clock,
            @Value("${improv.quests.daily-soft-cap}") long dailySoftCap) {
        this.userStatsRepository = userStatsRepository;
        this.daySummaryRepository = daySummaryRepository;
        this.levelCurveRepository = levelCurveRepository;
        this.rankTierRepository = rankTierRepository;
        this.streakService = streakService;
        this.clock = clock;
        this.dailySoftCap = dailySoftCap;
    }

    private record LevelProgress(int level, long xpIntoLevel, long xpForNextLevel) {
    }

    @Transactional
    public ProgressionResponse progression(User user) {
        UserStats stats = statsFor(user);
        LevelProgress progress = levelProgress(stats.getTotalXp());
        long xpToday = daySummaryRepository
                .findByUserIdAndQuestDate(user.getId(), LocalDate.now(clock.withZone(StreakService.zoneFor(user))))
                .map(DaySummary::getXpEarned)
                .orElse(0L);
        String color = rankTierRepository.findByRank(stats.getRank())
                .map(RankTier::getColor)
                .orElse("#8a8f98");
        return new ProgressionResponse(
                progress.level(),
                stats.getRank(),
                color,
                stats.getTotalXp(),
                progress.xpIntoLevel(),
                progress.xpForNextLevel(),
                stats.getCurrentStreak(),
                stats.getBestStreak(),
                xpToday,
                dailySoftCap,
                Math.max(0, dailySoftCap - xpToday));
    }

    /** Loads the user's stats row, creating it (with derived values) if absent. */
    @Transactional
    public UserStats statsFor(User user) {
        UserStats stats = userStatsRepository.findByUserId(user.getId())
                .orElseGet(() -> UserStats.builder().user(user).build());
        if (stats.getRank() == null) {
            applyLevelAndRank(stats);
            userStatsRepository.save(stats);
        }
        return stats;
    }

    /** Recomputes level, rank and both streaks, and persists the cache. */
    @Transactional
    public UserStats applyDerived(User user, UserStats stats) {
        applyLevelAndRank(stats);
        StreakService.Streaks streaks = streakService.compute(user);
        stats.setCurrentStreak(streaks.current());
        stats.setBestStreak(streaks.best());
        return userStatsRepository.save(stats);
    }

    @Transactional
    public UserStats refresh(User user) {
        return applyDerived(user, statsFor(user));
    }

    public List<RankInfo> ranks() {
        return rankTierRepository.findAllByOrderByMinLevelAsc().stream()
                .map(t -> new RankInfo(t.getRank(), t.getMinLevel(), t.getMaxLevel(), t.getColor(), t.getXpMultiplier()))
                .toList();
    }

    /** Walks the level curve: how far a total XP amount gets you. */
    private LevelProgress levelProgress(long totalXp) {
        List<LevelCurve> curve = levelCurveRepository.findAllByOrderByLevelAsc();
        int level = 1;
        long remaining = totalXp;
        for (LevelCurve row : curve) {
            if (row.getLevel() != level) {
                break; // contiguity guard: the curve must start at 1, no gaps
            }
            if (remaining < row.getXpToNext()) {
                break; // not enough XP for this level yet
            }
            remaining -= row.getXpToNext();
            level++;
        }
        long xpForNext;
        if (curve.isEmpty()) {
            xpForNext = 0;
        } else if (level <= curve.size()) {
            xpForNext = curve.get(level - 1).getXpToNext();
        } else {
            xpForNext = curve.get(curve.size() - 1).getXpToNext(); // past the curve: hold
        }
        return new LevelProgress(level, remaining, xpForNext);
    }

    private void applyLevelAndRank(UserStats stats) {
        LevelProgress progress = levelProgress(stats.getTotalXp());
        stats.setLevel(progress.level());
        stats.setRank(rankForLevel(progress.level()));
    }

    private String rankForLevel(int level) {
        for (RankTier tier : rankTierRepository.findAllByOrderByMinLevelAsc()) {
            if (level >= tier.getMinLevel() && (tier.getMaxLevel() == null || level <= tier.getMaxLevel())) {
                return tier.getRank();
            }
        }
        return "E";
    }
}
