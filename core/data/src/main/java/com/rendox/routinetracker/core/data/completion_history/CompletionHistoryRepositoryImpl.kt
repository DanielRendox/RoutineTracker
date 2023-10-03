package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.coroutines.flow.Flow

class CompletionHistoryRepositoryImpl(
    private val localDataSource: CompletionHistoryLocalDataSource,
) : CompletionHistoryRepository {

    override suspend fun getHistoryEntryByIndex(
        routineId: Long,
        numberOfDateFromRoutineStart: Long
    ): HistoricalStatus? {
        return localDataSource.getHistoryEntryByIndex(routineId, numberOfDateFromRoutineStart)
    }

    override fun getHistoryEntriesByIndices(
        routineId: Long,
        dateFromRoutineStartIndices: LongRange
    ): Flow<List<HistoricalStatus>> {
        return localDataSource.getHistoryEntriesByIndices(routineId, dateFromRoutineStartIndices)
    }

    override suspend fun insertHistoryEntry(
        numberOfDateFromRoutineStart: Long,
        routineId: Long,
        status: HistoricalStatus,
    ) {
        localDataSource.insertHistoryEntry(
            numberOfDateFromRoutineStart = numberOfDateFromRoutineStart,
            routineId = routineId,
            status = status,
        )
    }
}