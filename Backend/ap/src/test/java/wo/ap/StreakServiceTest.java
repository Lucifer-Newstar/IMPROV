package wo.ap;

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

/**
 * Streak derivation with fixed clocks — including the timezone cases that make
 * streak bugs (the P0 exit criterion: day boundaries must follow the user's
 * zone, not the server's).
 */
@DataJpaTest
@ActiveProfiles("test")
class StreakServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    DaySummaryRepository daySummaryRepository;

    StreakService service;
    User user;

    @BeforeEach
    void setUp() {
        // A fixed "now": 2026-10-09 12:00 UTC.
        Clock fixed = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneId.of("UTC"));
        service = new StreakService(daySummaryRepository, fixed);
        user = userRepository.save(User.builder()
                .firstname("Test")
                .username("streak")
                .password("hashed")
                .timezone("UTC")
                .build());
    }

    void active(LocalDate date) {
        daySummaryRepository.save(DaySummary.builder()
                .user(user)
                .questDate(date)
                .xpEarned(50L)
                .questsCompleted(1L)
                .build());
    }

    @Test
    void noDaysIsZeroStreak() {
        assertThat(service.compute(user)).isEqualTo(new StreakService.Streaks(0, 0));
    }

    @Test
    void activeTodayIsAOneDayStreak() {
        active(LocalDate.of(2026, 10, 9));
        assertThat(service.compute(user)).isEqualTo(new StreakService.Streaks(1, 1));
    }

    @Test
    void streakSurvivesUntilTodayEnds() {
        // Last active day was yesterday: the streak is still alive, because
        // today may simply not be finished yet.
        active(LocalDate.of(2026, 10, 7));
        active(LocalDate.of(2026, 10, 8));
        assertThat(service.compute(user)).isEqualTo(new StreakService.Streaks(2, 2));
    }

    @Test
    void aMissedDayInThePastBreaksTheStreak() {
        active(LocalDate.of(2026, 10, 5));
        active(LocalDate.of(2026, 10, 6));
        // Oct 7 and 8 fully passed with nothing logged.
        assertThat(service.compute(user)).isEqualTo(new StreakService.Streaks(0, 2));
    }

    @Test
    void bestStreakKeepsTheLongestRun() {
        active(LocalDate.of(2026, 10, 1));
        active(LocalDate.of(2026, 10, 2));
        active(LocalDate.of(2026, 10, 3));
        active(LocalDate.of(2026, 10, 9)); // gap on Oct 4-8
        assertThat(service.compute(user)).isEqualTo(new StreakService.Streaks(1, 3));
    }

    @Test
    void todayIsComputedInTheUsersTimezone() {
        // 2026-10-09T20:00Z is 2026-10-10 01:30 in Asia/Kolkata but still
        // 2026-10-09 in UTC — the same instant, two different "todays".
        Clock fixed = Clock.fixed(Instant.parse("2026-10-09T20:00:00Z"), ZoneId.of("UTC"));
        StreakService svc = new StreakService(daySummaryRepository, fixed);

        User ist = userRepository.save(User.builder()
                .firstname("T").username("istuser").password("x").timezone("Asia/Kolkata").build());
        daySummaryRepository.save(DaySummary.builder().user(ist)
                .questDate(LocalDate.of(2026, 10, 8)).xpEarned(10L).questsCompleted(1L).build());
        // In IST, Oct 8 is two days ago (local today is Oct 10): broken.
        assertThat(svc.compute(ist).current()).isZero();

        User utc = userRepository.save(User.builder()
                .firstname("T").username("utcuser").password("x").timezone("UTC").build());
        daySummaryRepository.save(DaySummary.builder().user(utc)
                .questDate(LocalDate.of(2026, 10, 8)).xpEarned(10L).questsCompleted(1L).build());
        // In UTC, Oct 8 was yesterday: still alive.
        assertThat(svc.compute(utc).current()).isEqualTo(1);
    }
}
