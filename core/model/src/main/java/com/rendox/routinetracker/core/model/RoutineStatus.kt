package com.rendox.routinetracker.core.model

sealed interface RoutineStatus

enum class PlanningStatus : RoutineStatus {
    Planned,

    /**
     * The user didn't complete the routine on one of the previous days, but backlog
     * is enabled so it's due on the given date.
     */
    Backlog,

    /**
     * The user over completed the routine on one of the previous days, or completed
     * it when it wasn't due, and the corresponding preference is enabled, which results
     * in this routine not being due on the given date.
     */
    AlreadyCompleted,

    /** The routine is not due because of its frequency */
    NotDue,

    OnVacation;
}

enum class HistoricalStatus : RoutineStatus {
    /** The user failed to complete the routine on the given date */
    NotCompleted,

    PartiallyCompleted,
    FullyCompleted,

    /** The user completed even more than planned */
    OverCompleted,

    /** The user didn't complete the day and didn't need to, thanks to the habit's frequency */
    Skipped,

    OnVacation;
}