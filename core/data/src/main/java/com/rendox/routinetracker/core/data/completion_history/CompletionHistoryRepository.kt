package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.coroutines.flow.Flow

interface CompletionHistoryRepository {

    fun getHistoryEntriesByIndices(
        routineId: Long,
        dateFromRoutineStartIndices: LongRange,
    ): Flow<List<HistoricalStatus>>

    suspend fun getHistoryEntryByIndex(
        routineId: Long,
        numberOfDateFromRoutineStart: Long,
    ): HistoricalStatus?

    suspend fun insertHistoryEntry(
        numberOfDateFromRoutineStart: Long,
        routineId: Long,
        status: HistoricalStatus,
    )
}