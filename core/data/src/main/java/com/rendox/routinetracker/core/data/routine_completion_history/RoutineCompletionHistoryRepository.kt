package com.rendox.routinetracker.core.data.routine_completion_history

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.datetime.LocalDate

interface RoutineCompletionHistoryRepository {

    suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange,
    ): List<CompletionHistoryEntry>

    suspend fun getHistoryEntryByDate(
        routineId: Long,
        date: LocalDate,
    ): CompletionHistoryEntry?

    suspend fun insertHistoryEntry(
        id: Long? = null,
        routineId: Long,
        entry: CompletionHistoryEntry,
    )

    suspend fun deleteHistoryEntry(
        routineId: Long,
        date: LocalDate,
    )

    suspend fun updateHistoryEntryByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus? = null,
        newScheduleDeviation: Float? = null,
        newTimesCompleted: Float? = null,
    )

    suspend fun getFirstHistoryEntry(routineId: Long): CompletionHistoryEntry?
    suspend fun getLastHistoryEntry(routineId: Long): CompletionHistoryEntry?

    suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean
    suspend fun deleteCompletedLaterBackupEntry(routineId: Long, date: LocalDate)

    suspend fun getFirstHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): CompletionHistoryEntry?

    suspend fun getLastHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): CompletionHistoryEntry?

    suspend fun getTotalTimesCompletedInPeriod(
        routineId: Long, startDate: LocalDate, endDate: LocalDate
    ): Double

    suspend fun getScheduleDeviationInPeriod(
        routineId: Long, startDate: LocalDate, endDate: LocalDate
    ): Double
}