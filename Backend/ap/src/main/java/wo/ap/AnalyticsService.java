package wo.ap;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only analytics over day_summaries and completed user_quests
 * (mechanics spec §6.6 — every view is a query, never a maintained counter).
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int CONSISTENCY_DAYS = 84; // 12 weeks of squares

    private final DaySummaryRepository daySummaryRepository;
    private final UserQuestRepository userQuestRepository;
    private final StreakService streakService;
    private final Clock clock;

    public OverviewResponse overview(User user) {
        LocalDate today = LocalDate.now(clock.withZone(StreakService.zoneFor(user)));
        Map<LocalDate, DaySummary> byDate = daySummaryRepository
                .findByUserIdAndQuestDateBetweenOrderByQuestDateAsc(user.getId(), today.minusDays(CONSISTENCY_DAYS - 1), today)
                .stream()
                .collect(Collectors.toMap(DaySummary::getQuestDate, d -> d));

        List<DayXp> weekly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            weekly.add(new DayXp(date, xpOf(byDate.get(date))));
        }

        List<DayActivity> consistency = new ArrayList<>();
        for (int i = CONSISTENCY_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            consistency.add(activityOf(byDate.get(date)));
        }

        long str = 0;
        long agi = 0;
        long vit = 0;
        for (Object[] row : userQuestRepository.sumStatAmountByUserIdGroupedByStatCode(user.getId())) {
            String code = (String) row[0];
            long amount = ((Number) row[1]).longValue();
            if ("STR".equals(code)) {
                str = amount;
            } else if ("AGI".equals(code)) {
                agi = amount;
            } else if ("VIT".equals(code)) {
                vit = amount;
            }
        }

        return new OverviewResponse(
                weekly,
                consistency,
                new StatTotals(str, agi, vit),
                userQuestRepository.sumLoggedByUserIdAndCompletedType(user.getId(), QuestType.REPS),
                userQuestRepository.countByUserIdAndCompletedTrue(user.getId()),
                daySummaryRepository.countByUserIdAndQuestsCompletedGreaterThan(user.getId(), 0));
    }

    public StreakResponse streak(User user) {
        StreakService.Streaks streaks = streakService.compute(user);
        List<DayActivity> days = daySummaryRepository
                .findByUserIdAndQuestsCompletedGreaterThanOrderByQuestDateDesc(user.getId(), 0)
                .stream()
                .map(d -> new DayActivity(d.getQuestDate(), true, d.getXpEarned(), d.getQuestsCompleted()))
                .toList();
        return new StreakResponse(streaks.current(), streaks.best(), days);
    }

    /** One month of days for the streak calendar. month is "YYYY-MM"; null = current. */
    public CalendarResponse calendar(User user, String month) {
        YearMonth ym = parseMonth(user, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        Map<LocalDate, DaySummary> byDate = daySummaryRepository
                .findByUserIdAndQuestDateBetweenOrderByQuestDateAsc(user.getId(), start, end)
                .stream()
                .collect(Collectors.toMap(DaySummary::getQuestDate, d -> d));
        List<DayActivity> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            days.add(activityOf(byDate.get(date)));
        }
        return new CalendarResponse(ym.toString(), days);
    }

    private YearMonth parseMonth(User user, String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now(clock.withZone(StreakService.zoneFor(user)));
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "month must be YYYY-MM");
        }
    }

    private static DayActivity activityOf(DaySummary summary) {
        if (summary == null) {
            return new DayActivity(null, false, 0, 0);
        }
        return new DayActivity(summary.getQuestDate(), summary.getQuestsCompleted() > 0,
                summary.getXpEarned(), summary.getQuestsCompleted());
    }

    private static long xpOf(DaySummary summary) {
        return summary == null ? 0 : summary.getXpEarned();
    }
}
