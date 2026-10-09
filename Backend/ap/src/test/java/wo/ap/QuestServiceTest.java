package wo.ap;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The core loop: board generation, tap-to-log, exactly-once XP awarding, the
 * daily soft cap and the plausibility bounds. H2 + fixed clock, no services.
 */
@DataJpaTest
@ActiveProfiles("test")
class QuestServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserStatsRepository userStatsRepository;
    @Autowired
    UserQuestRepository userQuestRepository;
    @Autowired
    QuestTemplateRepository questTemplateRepository;
    @Autowired
    DaySummaryRepository daySummaryRepository;
    @Autowired
    XpLedgerRepository xpLedgerRepository;
    @Autowired
    RankTierRepository rankTierRepository;
    @Autowired
    LevelCurveRepository levelCurveRepository;

    QuestService service;
    ProgressionService progressionService;
    User user;

    @BeforeEach
    void setUp() {
        Clock fixed = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneId.of("UTC"));
        StreakService streakService = new StreakService(daySummaryRepository, fixed);
        progressionService = new ProgressionService(userStatsRepository, daySummaryRepository,
                levelCurveRepository, rankTierRepository, streakService, fixed, 400);
        service = new QuestService(userQuestRepository, questTemplateRepository, userStatsRepository,
                daySummaryRepository, xpLedgerRepository, rankTierRepository, progressionService,
                fixed, 5, 400, new SimpleMeterRegistry());

        for (int level = 1; level <= 200; level++) {
            levelCurveRepository.save(new LevelCurve(level, 100L + 25L * (level - 1)));
        }
        rankTierRepository.save(new RankTier("E", 1, 9, "#8a8f98", 1.0));
        rankTierRepository.save(new RankTier("D", 10, 24, "#4ade80", 1.1));

        template("Push-ups", QuestType.REPS, 100, 50, 1);
        template("Squats", QuestType.REPS, 60, 50, 1);
        template("Plank", QuestType.DURATION, 180, 40, 1);
        template("Run", QuestType.DURATION, 1200, 60, 1);
        template("Workout", QuestType.WORKOUT, 1, 100, 1);
        template("Lunges", QuestType.REPS, 40, 40, 5);
        template("Burpees", QuestType.REPS, 30, 50, 10);

        user = userRepository.save(User.builder()
                .firstname("Q").username("quester").password("hashed").timezone("UTC").build());
        userStatsRepository.save(UserStats.builder().user(user).build());
    }

    QuestTemplate template(String name, QuestType type, long target, long xp, int minLevel) {
        return questTemplateRepository.save(QuestTemplate.builder()
                .name(name)
                .description(name + " description")
                .type(type)
                .target(target)
                .unit("reps")
                .statCode("STR")
                .statAmount(10L)
                .xpReward(xp)
                .minLevel(minLevel)
                .build());
    }

    UserQuestDto onlyQuestOfType(QuestBoardResponse board, String type) {
        return board.quests().stream()
                .filter(q -> type.equals(q.type()))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void boardIsGeneratedFromTemplatesTheLevelUnlocks() {
        QuestBoardResponse board = service.todayBoard(user);
        assertThat(board.quests()).hasSize(5); // level 1 unlocks exactly five
        assertThat(board.quests()).allSatisfy(q -> {
            assertThat(q.name()).isNotBlank();
            assertThat(q.xpReward()).isPositive();
            assertThat(q.logged()).isZero();
            assertThat(q.completed()).isFalse();
        });
    }

    @Test
    void boardGenerationIsIdempotentForTheSameDay() {
        QuestBoardResponse first = service.todayBoard(user);
        QuestBoardResponse second = service.todayBoard(user);
        assertThat(second.quests().stream().map(UserQuestDto::id).toList())
                .containsExactlyElementsOf(first.quests().stream().map(UserQuestDto::id).toList());
    }

    @Test
    void completingAQuestAwardsXpAndLevelsUp() {
        QuestBoardResponse board = service.todayBoard(user);
        UserQuestDto done = service.complete(user, onlyQuestOfType(board, "WORKOUT").id());
        assertThat(done.completed()).isTrue();
        assertThat(done.awardedXp()).isEqualTo(100); // rank E multiplier is x1.0

        ProgressionResponse p = progressionService.progression(user);
        assertThat(p.totalXp()).isEqualTo(100);
        assertThat(p.level()).isEqualTo(2); // 100 XP is exactly L1 -> L2
    }

    @Test
    void completingTwiceDoesNotDoubleAward() {
        QuestBoardResponse board = service.todayBoard(user);
        long id = onlyQuestOfType(board, "WORKOUT").id();
        service.complete(user, id);
        UserQuestDto again = service.complete(user, id);
        assertThat(again.completed()).isTrue();
        assertThat(xpLedgerRepository.findAll()).hasSize(1); // exactly-once via the ledger
        assertThat(progressionService.progression(user).totalXp()).isEqualTo(100);
    }

    @Test
    void tapToLogAccumulatesAndCompletesAtTarget() {
        QuestBoardResponse board = service.todayBoard(user);
        long id = onlyQuestOfType(board, "REPS").id(); // Push-ups, target 100, 50 XP

        UserQuestDto partial = service.log(user, id, 40);
        assertThat(partial.logged()).isEqualTo(40);
        assertThat(partial.completed()).isFalse();

        UserQuestDto done = service.log(user, id, 80); // 40 + 80 = 120 -> clamped to 100
        assertThat(done.logged()).isEqualTo(100);
        assertThat(done.completed()).isTrue();
        assertThat(done.awardedXp()).isEqualTo(50);
    }

    @Test
    void amountAboveThePlausibilityBoundIsRejected() {
        QuestBoardResponse board = service.todayBoard(user);
        long id = onlyQuestOfType(board, "REPS").id();
        assertThatThrownBy(() -> service.log(user, id, 501))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("max 500");
    }

    @Test
    void dailySoftCapHalvesAwardsPastTheCap() {
        // The day already has 400 XP: the next award is halved (400 -> cap).
        daySummaryRepository.save(DaySummary.builder()
                .user(user)
                .questDate(LocalDate.of(2026, 10, 9))
                .xpEarned(400L)
                .questsCompleted(4L)
                .build());
        QuestBoardResponse board = service.todayBoard(user);
        UserQuestDto done = service.complete(user, onlyQuestOfType(board, "WORKOUT").id());
        assertThat(done.awardedXp()).isEqualTo(50); // 100 halved
    }

    @Test
    void questsOfOtherUsersAreNotFound() {
        User other = userRepository.save(User.builder()
                .firstname("O").username("other").password("x").timezone("UTC").build());
        userStatsRepository.save(UserStats.builder().user(other).build());
        QuestBoardResponse board = service.todayBoard(user);
        long questId = board.quests().get(0).id();
        assertThatThrownBy(() -> service.complete(other, questId))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not found");
    }
}
