package wo.ap;

import java.util.List;

/** The analytics screen: last 7 days of XP, 12 weeks of consistency, totals. */
public record OverviewResponse(
        List<DayXp> weeklyXp,
        List<DayActivity> consistency,
        StatTotals stats,
        long totalReps,
        long questsCompleted,
        long activeDays
) {
}
