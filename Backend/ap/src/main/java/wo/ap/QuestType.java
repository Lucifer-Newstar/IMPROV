package wo.ap;

/** How a quest is logged (mechanics spec §6.5). */
public enum QuestType {
    /** Incremental rep counting (tap-to-log 5 / 10 / 20). */
    REPS,
    /** Timed effort, target in seconds. */
    DURATION,
    /** Incremental set counting. */
    SETS,
    /** A single completion event (e.g. "log a workout session"). */
    WORKOUT,
    /** Free-form completion. */
    CUSTOM
}
