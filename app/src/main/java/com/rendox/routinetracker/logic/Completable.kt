package com.rendox.routinetracker.logic

interface CompletableStatus

enum class PlanningStatus : CompletableStatus {
    Planned,
    Backlog,
    NotDue;
}

enum class CompletionStatus : CompletableStatus {
    PartiallyCompleted,
    FullyCompleted,
}

interface Completable