package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.datetime.LocalDate

class CompletionHistoryRepositoryImpl(
    private val localDataSource: CompletionHistoryLocalDataSource,
) : CompletionHistoryRepository {

    override suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange
    ): List<CompletionHistoryEntry> {
        return localDataSource.getHistoryEntries(routineId, dates)
    }

    override suspend fun getHistoryEntryByDate(
        routineId: Long,
        date: LocalDate
    ): CompletionHistoryEntry? {
        return localDataSource.getHistoryEntryByDate(routineId, date)
    }

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
    ) {
        localDataSource.insertHistoryEntry(
            id = id,
            routineId = routineId,
            entry = entry,
        )
    }

    override suspend fun deleteHistoryEntry(routineId: Long, date: LocalDate) {
        localDataSource.deleteHistoryEntry(routineId, date)
    }

    override suspend fun updateHistoryEntryByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus?,
        newScheduleDeviation: Float?,
        newTimesCompleted: Float?,
    ) {
        localDataSource.updateHistoryEntryByDate(
            routineId = routineId,
            date = date,
            newStatus = newStatus,
            newScheduleDeviation = newScheduleDeviation,
            newTimesCompleted = newTimesCompleted,
        )
    }

    override suspend fun getFirstHistoryEntry(routineId: Long): CompletionHistoryEntry? {
        return localDataSource.getFirstHistoryEntry(routineId)
    }

    override suspend fun getLastHistoryEntry(routineId: Long): CompletionHistoryEntry? {
        return localDataSource.getLastHistoryEntry(routineId)
    }

    override suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean {
        return localDataSource.checkIfStatusWasCompletedLater(routineId, date)
    }

    override suspend fun deleteCompletedLaterBackupEntry(routineId: Long, date: LocalDate) {
        localDataSource.deleteCompletedLaterBackupEntry(routineId, date)
    }

    override suspend fun getFirstHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): CompletionHistoryEntry? {
        return localDataSource.getFirstHistoryEntryByStatus(
            routineId = routineId,
            matchingStatuses = matchingStatuses,
            minDate = minDate,
            maxDate = maxDate,
        )
    }

    override suspend fun getLastHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): CompletionHistoryEntry? = localDataSource.getLastHistoryEntryByStatus(
        routineId = routineId,
        matchingStatuses = matchingStatuses,
        minDate = minDate,
        maxDate = maxDate,
    )

    override suspend fun getTotalTimesCompletedInPeriod(
        routineId: Long, startDate: LocalDate, endDate: LocalDate
    ): Double {
        return localDataSource.getTotalTimesCompletedInPeriod(
            routineId, startDate, endDate
        )
    }

    override suspend fun getScheduleDeviationInPeriod(
        routineId: Long, startDate: LocalDate, endDate: LocalDate
    ): Double {
        return localDataSource.getScheduleDeviationInPeriod(
            routineId, startDate, endDate
        )
    }
}