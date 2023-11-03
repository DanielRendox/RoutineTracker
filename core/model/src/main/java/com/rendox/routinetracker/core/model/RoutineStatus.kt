package com.rendox.routinetracker.core.model

sealed interface RoutineStatus

enum class PlanningStatus : RoutineStatus {

    /** The routine is due on this day because of its frequency */
    Planned,

    /**
     * Although the routine is not due on this day,
     * there is some backlog that needs to be dealt with.
     */
    Backlog,

    /**
     * Although the routine is due on this day, the user may skip it
     * because they had over completed one of the days earlier.
     */
    AlreadyCompleted,

    /** The routine is not due on this day because of its frequency */
    NotDue,

    /** The routine is not due on this day because it's currently on vacation */
    OnVacation;
}

enum class HistoricalStatus : RoutineStatus {

    /**
     * The routine was due on the day but the user failed to complete it.
     */
    NotCompleted,

    /**
     * The routine was due on the day and the user completed it.
     */
    Completed,

    /**
     * The routine wasn't due on the day but the user still completed it.
     */
    OverCompleted,

    /**
     * The routine wasn't due on the day because it was currently
     * on vacation but the user still completed the day.
     */
    OverCompletedOnVacation,

    /**
     * The routine wasn't due on the day because it was currently
     * on vacation but the user still sorted out some backlog.
     */
    SortedOutBacklogOnVacation,

    /** The user didn't complete the day and didn't need to, thanks to the routine's frequency */
    Skipped,

    /**
     * The user didn't complete the day and didn't need to,
     * because the routine was currently on vacation.
     */
    NotCompletedOnVacation,

    /**
     * The routine was due but the user failed to complete it on that day.
     * However, they caught up on this backlog later.
     */
    CompletedLater,

    /**
     * The routine wasn't due on the day, but there was a backlog and the user completed it.
     */
    SortedOutBacklog,

    /**
     * Although the routine was due on this day, the user skipped it
     * because they had over completed one of the days earlier.
     */
    AlreadyCompleted,
}