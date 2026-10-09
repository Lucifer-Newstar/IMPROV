package wo.ap;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Tap-to-log amount. The service applies the per-type plausibility bound on
 * top of this (reps ≤ 500, duration ≤ 3600s per call — anti-abuse).
 */
public record LogQuestRequest(

        @NotNull @Min(1) @Max(86400) Long amount
) {
}
