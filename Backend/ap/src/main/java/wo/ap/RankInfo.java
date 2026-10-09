package wo.ap;

/** One tier of the rank ladder, for the rank screen. */
public record RankInfo(
        String rank,
        int minLevel,
        Integer maxLevel,
        String color,
        double xpMultiplier
) {
}
