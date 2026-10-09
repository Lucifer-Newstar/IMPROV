package wo.ap;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Today's quest board. resetAt is the next local midnight in the user's timezone. */
public record QuestBoardResponse(
        LocalDate questDate,
        Instant resetAt,
        List<UserQuestDto> quests
) {
}
