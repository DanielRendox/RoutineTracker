package com.rendox.routinetracker.core.data.completiontime

import com.rendox.routinetracker.core.database.completiontime.DueDateSpecificCompletionTimeLocalDataSource
import kotlinx.datetime.LocalTime

class DueDateSpecificCompletionTimeRepositoryImpl(
    private val localDataSource: DueDateSpecificCompletionTimeLocalDataSource,
) : DueDateSpecificCompletionTimeRepository {
    override suspend fun getDueDateSpecificCompletionTime(
        scheduleId: Long,
        dueDateNumber: Int,
    ) = localDataSource.getDueDateSpecificCompletionTime(scheduleId, dueDateNumber)

    override suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime,
        scheduleId: Long,
        dueDateNumber: Int,
    ) = localDataSource.updateDueDateSpecificCompletionTime(newTime, scheduleId, dueDateNumber)
}