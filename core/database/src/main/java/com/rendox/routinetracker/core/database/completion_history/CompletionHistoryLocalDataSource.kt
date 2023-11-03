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
        scheduleDeviationIncrementAmount: Int,
    )

    suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        status: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int,
    )

    suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int,
        matchingStatuses: List<HistoricalStatus>,
    )

    suspend fun getFirstHistoryEntryDate(routineId: Long): LocalDate?
    suspend fun getLastHistoryEntryDate(routineId: Long): LocalDate?

    suspend fun getFirstHistoryEntryDateByStatus(
        routineId: Long,
        startingFromDate: LocalDate,
        matchingStatuses: List<HistoricalStatus>,
    ): LocalDate?

}