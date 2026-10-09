package wo.ap;

/** The hunter card: everything the home screen needs to render progression. */
public record ProgressionResponse(
        int level,
        String rank,
        String rankColor,
        long totalXp,
        long xpIntoLevel,
        long xpForNextLevel,
        long currentStreak,
        long bestStreak,
        long xpToday,
        long dailySoftCap,
        long xpRemainingToday
) {
}
