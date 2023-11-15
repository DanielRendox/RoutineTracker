package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

data class StatusEntry(
    val date: LocalDate,
    val status: RoutineStatus,
)

data class CompletionHistoryEntry(
    val date: LocalDate,
    val status: HistoricalStatus,
    val currentScheduleDeviation: Int,
)

fun CompletionHistoryEntry.toStatusEntry() = StatusEntry(
    date = date,
    status = status,
)
