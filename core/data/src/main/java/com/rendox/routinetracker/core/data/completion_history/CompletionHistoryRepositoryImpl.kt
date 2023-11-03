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
        scheduleDeviationIncrementAmount: Int,
    ) {
        localDataSource.insertHistoryEntry(
            id = id,
            routineId = routineId,
            entry = entry,
            scheduleDeviationIncrementAmount = scheduleDeviationIncrementAmount,
        )
    }

    override suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        status: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int,
    ) {
        localDataSource.updateHistoryEntryStatusByDate(
            routineId = routineId,
            date = date,
            status = status,
            scheduleDeviationIncrementAmount = scheduleDeviationIncrementAmount,
        )
    }

    override suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int,
        matchingStatuses: List<HistoricalStatus>
    ) {
        localDataSource.updateHistoryEntryStatusByStatus(
            routineId = routineId,
            newStatus = newStatus,
            scheduleDeviationIncrementAmount = scheduleDeviationIncrementAmount,
            matchingStatuses = matchingStatuses,
        )
    }

    override suspend fun getFirstHistoryEntryDate(routineId: Long): LocalDate? {
        return localDataSource.getFirstHistoryEntryDate(routineId)
    }

    override suspend fun getLastHistoryEntryDate(routineId: Long): LocalDate? {
        return localDataSource.getLastHistoryEntryDate(routineId)
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
}