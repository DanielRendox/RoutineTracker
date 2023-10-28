package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.datetime.LocalDate

interface CompletionHistoryLocalDataSource {

    suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange,
    ): List<CompletionHistoryEntry>

    suspend fun insertHistoryEntry(
        id: Long? = null,
        routineId: Long,
        entry: CompletionHistoryEntry,
        tasksCompletedCounterIncrementAmount: Int?,
    )

    suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        status: HistoricalStatus,
        tasksCompletedCounterIncrementAmount: Int?,
    )

    suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        tasksCompletedCounterIncrementAmount: Int?,
        matchingStatuses: List<HistoricalStatus>,
    )

    suspend fun getLastHistoryEntryDate(routineId: Long): LocalDate?

    suspend fun countDaysThatMatchStatusInPeriodRange(
        status: HistoricalStatus,
        period: LocalDateRange,
    ): Int
}