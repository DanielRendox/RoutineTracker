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

    override suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus,
        newScheduleDeviation: Int,
    ) {
        localDataSource.updateHistoryEntryStatusByDate(
            routineId = routineId,
            date = date,
            newStatus = newStatus,
            newScheduleDeviation = newScheduleDeviation,
        )
    }

    override suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        newScheduleDeviation: Int,
        matchingStatuses: List<HistoricalStatus>,
    ) {
        localDataSource.updateHistoryEntryStatusByStatus(
            routineId = routineId,
            newStatus = newStatus,
            newScheduleDeviation = newScheduleDeviation,
            matchingStatuses = matchingStatuses,
        )
    }

    override suspend fun getFirstHistoryEntry(routineId: Long): CompletionHistoryEntry? {
        return localDataSource.getFirstHistoryEntry(routineId)
    }

    override suspend fun getLastHistoryEntry(routineId: Long): CompletionHistoryEntry? {
        return localDataSource.getLastHistoryEntry(routineId)
    }

    override suspend fun getFirstHistoryEntryDateByStatus(
        routineId: Long,
        startingFromDate: LocalDate,
        matchingStatuses: List<HistoricalStatus>
    ): LocalDate? {
        return localDataSource.getFirstHistoryEntryDateByStatus(
            routineId, startingFromDate, matchingStatuses
        )
    }

    override suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean {
        return localDataSource.checkIfStatusWasCompletedLater(routineId, date)
    }

    override suspend fun insertCompletedLaterDate(id: Long?, routineId: Long, date: LocalDate) {
        localDataSource.insertCompletedLaterDate(id, routineId, date)
    }

    override suspend fun deleteCompletedLaterDate(routineId: Long, date: LocalDate) {
        localDataSource.deleteCompletedLaterDate(routineId, date)
    }

    override suspend fun findLastHistoryEntryDateByStatus(
        routineId: Long, matchingStatuses: List<HistoricalStatus>
    ): CompletionHistoryEntry? = localDataSource.findLastHistoryEntryDateByStatus(routineId, matchingStatuses)
}