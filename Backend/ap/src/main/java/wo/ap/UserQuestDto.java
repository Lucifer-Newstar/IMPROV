package wo.ap;

/** A user's quest for a day, as the quest board renders it. */
public record UserQuestDto(
        long id,
        Long templateId,
        String name,
        String description,
        String type,
        long target,
        String unit,
        String statCode,
        Long statAmount,
        long xpReward,
        Long awardedXp,
        long logged,
        boolean completed
) {
}
