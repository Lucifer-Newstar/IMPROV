package wo.ap;

import java.time.LocalDate;

/** One day for calendars, consistency grids and timelines. */
public record DayActivity(
        LocalDate date,
        boolean active,
        long xp,
        long quests
) {
}
