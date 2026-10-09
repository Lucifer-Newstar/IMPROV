package wo.ap;

import java.util.List;

/** Streak numbers plus every active day (newest first) for the timeline. */
public record StreakResponse(
        long currentStreak,
        long bestStreak,
        List<DayActivity> days
) {
}
