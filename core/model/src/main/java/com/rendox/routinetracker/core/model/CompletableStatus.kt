package com.rendox.routinetracker.core.model

interface CompletableStatus

enum class PlanningStatus : CompletableStatus {
    /** No data is available for the given date */
    Unknown,
    Planned,
    NotDue;
}

enum class HistoricalStatus : CompletableStatus {
    NotCompleted,
    PartiallyCompleted,
    FullyCompleted,
    /** The user completed even more than planned */
    OverCompleted,
    /** The user didn't complete the day and didn't need to, thanks to the habit's frequency */
    Skipped,
    OnVacation;
}

interface Completable