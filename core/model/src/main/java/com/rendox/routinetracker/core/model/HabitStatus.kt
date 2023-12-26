package com.rendox.routinetracker.core.model

enum class HabitStatus {
    /** The habit is due on this day because of its frequency */
    Planned,

    /**
     * Although the habit is not due on this day,
     * there is some backlog that needs to be dealt with.
     */
    Backlog,

    /** The habit is not due on this day because of its frequency */
    NotDue,

    /** The user didn't complete the day and didn't need to, because of the habit's frequency. */
    Skipped,

    /**
     * Although the habit is due on this day, the user may skip it
     * because they had over completed one of the days earlier.
     */
    AlreadyCompleted,

    /**
     * The routine was due but the user failed to complete it on that day.
     * However, they caught up on this backlog later.
     */
    CompletedLater,

    /** The habit was due on this day and the user completed it. */
    Completed,

    /** The habit was due on this day. The user completed only a part of the planned amount. */
    PartiallyCompleted,

    /**
     * The routine wasn't due on the day but the user still completed it.
     */
    OverCompleted,

    /**
     * The routine wasn't due on the day, but there was a backlog and the user completed it.
     */
    SortedOutBacklog,

    /** The habit was due on this day but the user failed to complete it. */
    Failed,

    /** The habit is not due on this day because it's currently on vacation */
    OnVacation,

    /** The habit hasn't been started at the moment of the given date **/
    NotStarted,

    /** The habit has been finished at the moment of the given date. */
    Finished,
}