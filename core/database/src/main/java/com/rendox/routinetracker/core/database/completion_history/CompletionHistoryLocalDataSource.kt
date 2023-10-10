package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.coroutines.flow.Flow

interface CompletionHistoryLocalDataSource {

    fun getHistoryEntriesByIndices(
        routineId: Long,
        dateFromRoutineStartIndices: LongRange,
    ): Flow<List<HistoricalStatus>>

    fun getHistoryEntryByIndex(
        routineId: Long,
        numberOfDateFromRoutineStart: Long,
    ): Flow<HistoricalStatus?>

    suspend fun insertHistoryEntry(
        numberOfDateFromRoutineStart: Long,
        routineId: Long,
        status: HistoricalStatus,
    )
}