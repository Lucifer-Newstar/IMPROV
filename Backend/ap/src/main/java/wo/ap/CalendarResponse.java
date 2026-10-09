package wo.ap;

import java.util.List;

/** One month of days for the streak calendar. month is "YYYY-MM". */
public record CalendarResponse(
        String month,
        List<DayActivity> days
) {
}
