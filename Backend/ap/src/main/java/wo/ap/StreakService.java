package wo.ap;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Streak derivation (mechanics spec §6.4). Everything is computed from
 * day_summaries — a streak is never mutated directly.
 *
 * An active day is one with at least one completed quest. The current streak
 * is the consecutive run of active days ending at the most recent active day,
 * and it stays alive while that day is today or yesterday (today may simply
 * not be finished yet). A fully missed day in the past breaks it — rest-day
 * tokens, which protect a streak, are a P3 feature.
 */
@Service
@RequiredArgsConstructor
public class StreakService {

    private final DaySummaryRepository daySummaryRepository;
    private final Clock clock;

    public record Streaks(long current, long best) {
    }

    public Streaks compute(User user) {
        LocalDate today = LocalDate.now(clock.withZone(zoneFor(user)));
        List<LocalDate> active = daySummaryRepository.findByUserIdOrderByQuestDateAsc(user.getId())
                .stream()
                .filter(d -> d.getQuestsCompleted() > 0)
                .map(DaySummary::getQuestDate)
                .toList();
        if (active.isEmpty()) {
            return new Streaks(0, 0);
        }

        // Best streak: longest run of consecutive active days, ever.
        long best = 1;
        long run = 1;
        for (int i = 1; i < active.size(); i++) {
            if (active.get(i).equals(active.get(i - 1).plusDays(1))) {
                run++;
                best = Math.max(best, run);
            } else {
                run = 1;
            }
        }

        // Current streak: the run ending at the last active day, if that day
        // is today or yesterday (an older last active day means a full day
        // already passed with nothing logged — the streak is broken).
        LocalDate last = active.get(active.size() - 1);
        long current = 0;
        if (!last.isBefore(today.minusDays(1))) {
            current = 1;
            for (int i = active.size() - 2; i >= 0; i--) {
                if (active.get(i).equals(active.get(i + 1).minusDays(1))) {
                    current++;
                } else {
                    break;
                }
            }
        }
        return new Streaks(current, best);
    }

    /** The user's IANA zone, falling back to UTC for anything invalid. */
    public static ZoneId zoneFor(User user) {
        String tz = user.getTimezone();
        if (tz == null || tz.isBlank()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(tz);
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }
}
