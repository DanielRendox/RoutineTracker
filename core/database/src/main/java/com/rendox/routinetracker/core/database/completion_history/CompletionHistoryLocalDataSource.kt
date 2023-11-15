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
    )

    suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus,
        newScheduleDeviation: Int,
    )

    suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        newScheduleDeviation: Int,
        matchingStatuses: List<HistoricalStatus>,
    )

    suspend fun getFirstHistoryEntry(routineId: Long): CompletionHistoryEntry?
    suspend fun getLastHistoryEntry(routineId: Long): CompletionHistoryEntry?

    suspend fun getFirstHistoryEntryDateByStatus(
        routineId: Long,
        startingFromDate: LocalDate,
        matchingStatuses: List<HistoricalStatus>,
    ): LocalDate?

    suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean
    suspend fun insertCompletedLaterDate(id: Long? = null, routineId: Long, date: LocalDate)
    suspend fun deleteCompletedLaterDate(routineId: Long, date: LocalDate)

    suspend fun findLastHistoryEntryDateByStatus(
        routineId: Long, matchingStatuses: List<HistoricalStatus>
    ): CompletionHistoryEntry?

    suspend fun deleteHistoryEntry(routineId: Long, date: LocalDate)
}