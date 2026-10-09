package wo.ap;

import java.time.LocalDate;

/** XP earned on one day, for charts. */
public record DayXp(LocalDate date, long xp) {
}
