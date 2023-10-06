package com.rendox.routinetracker.core.database.completion_history

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CompletionHistoryLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : CompletionHistoryLocalDataSource {

    override fun getHistoryEntriesByIndices(
        routineId: Long,
        dateFromRoutineStartIndices: LongRange,
    ): Flow<List<HistoricalStatus>> {
        return db.completionHistoryEntityQueries.getHistoryEntriesByIndices(
            routineId = routineId,
            start = dateFromRoutineStartIndices.first,
            end = dateFromRoutineStartIndices.last,
        ).asFlow().mapToList(dispatcher).map {
            it.map { historyEntity -> historyEntity.status }
        }
    }

    override suspend fun getHistoryEntryByIndex(
        routineId: Long,
        numberOfDateFromRoutineStart: Long
    ): HistoricalStatus? {
        return db.completionHistoryEntityQueries.getHistoryEntryByIndex(
            routineId = routineId,
            numberOfDateFromRoutineStart = numberOfDateFromRoutineStart,
        ).executeAsOneOrNull()?.status
    }

    override suspend fun insertHistoryEntry(
        numberOfDateFromRoutineStart: Long,
        routineId: Long,
        status: HistoricalStatus,
    ) {
        db.completionHistoryEntityQueries.insertHistoryEntry(
            numberOfDateFromRoutineStart = numberOfDateFromRoutineStart,
            routineId = routineId,
            status = status,
        )
    }
}