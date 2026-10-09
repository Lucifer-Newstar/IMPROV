package wo.ap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Level and rank derivation against the seeded curve (mechanics spec §6.1/§6.2).
 * Runs on H2 with a fixed clock — no external services.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProgressionServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserStatsRepository userStatsRepository;
    @Autowired
    DaySummaryRepository daySummaryRepository;
    @Autowired
    LevelCurveRepository levelCurveRepository;
    @Autowired
    RankTierRepository rankTierRepository;

    ProgressionService service;

    @BeforeEach
    void setUp() {
        Clock fixed = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneId.of("UTC"));
        StreakService streakService = new StreakService(daySummaryRepository, fixed);
        service = new ProgressionService(userStatsRepository, daySummaryRepository,
                levelCurveRepository, rankTierRepository, streakService, fixed, 400);
        seedConfig();
    }

    void seedConfig() {
        for (int level = 1; level <= 200; level++) {
            levelCurveRepository.save(new LevelCurve(level, 100L + 25L * (level - 1)));
        }
        rankTierRepository.save(new RankTier("E", 1, 9, "#8a8f98", 1.0));
        rankTierRepository.save(new RankTier("D", 10, 24, "#4ade80", 1.1));
        rankTierRepository.save(new RankTier("C", 25, 49, "#35d0ff", 1.2));
        rankTierRepository.save(new RankTier("B", 50, 99, "#a78bfa", 1.35));
        rankTierRepository.save(new RankTier("A", 100, 199, "#f5c542", 1.5));
        rankTierRepository.save(new RankTier("S", 200, null, "#ff4f5b", 1.75));
    }

    User newUser(String username) {
        return userRepository.save(User.builder()
                .firstname("Test")
                .username(username)
                .password("hashed")
                .timezone("UTC")
                .build());
    }

    @Test
    void zeroXpIsLevelOneRankE() {
        User user = newUser("alice");
        userStatsRepository.save(UserStats.builder().user(user).build());
        ProgressionResponse p = service.progression(user);
        assertThat(p.level()).isEqualTo(1);
        assertThat(p.rank()).isEqualTo("E");
        assertThat(p.totalXp()).isZero();
        assertThat(p.xpIntoLevel()).isZero();
        assertThat(p.xpForNextLevel()).isEqualTo(100);
    }

    @Test
    void levelWalksTheCurve() {
        // 100 (L1->2) + 125 (L2->3) + 25 into L3 = 250 total XP
        User user = newUser("bob");
        userStatsRepository.save(UserStats.builder().user(user).totalXp(250L).build());
        ProgressionResponse p = service.progression(user);
        assertThat(p.level()).isEqualTo(3);
        assertThat(p.xpIntoLevel()).isEqualTo(25);
        assertThat(p.xpForNextLevel()).isEqualTo(150); // 100 + 25 x (3 - 1)
        assertThat(p.rank()).isEqualTo("E");
    }

    @Test
    void rankFollowsTheLadder() {
        // Cumulative XP to reach level 10 is exactly 1,800 (spec §6.1 table).
        User user = newUser("carol");
        userStatsRepository.save(UserStats.builder().user(user).totalXp(1800L).build());
        ProgressionResponse p = service.progression(user);
        assertThat(p.level()).isEqualTo(10);
        assertThat(p.rank()).isEqualTo("D");
    }

    @Test
    void ranksEndpointListsTheWholeLadder() {
        assertThat(service.ranks())
                .extracting(RankInfo::rank)
                .containsExactly("E", "D", "C", "B", "A", "S");
    }
}
