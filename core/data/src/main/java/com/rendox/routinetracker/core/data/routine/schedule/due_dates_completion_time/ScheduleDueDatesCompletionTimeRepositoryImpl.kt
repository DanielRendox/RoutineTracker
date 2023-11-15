package com.rendox.routinetracker.core.data.routine.schedule.due_dates_completion_time

import com.rendox.routinetracker.core.database.routine.schedule.due_dates_completion_time.ScheduleDueDatesCompletionTimeLocalDataSource
import kotlinx.datetime.LocalTime

class ScheduleDueDatesCompletionTimeRepositoryImpl(
    private val localDataSource: ScheduleDueDatesCompletionTimeLocalDataSource
) : ScheduleDueDatesCompletionTimeRepository {
    override suspend fun updateDueDateCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    ) {
        localDataSource.updateDueDateCompletionTime(time, routineId, dueDateNumber)
    }

    override suspend fun getDueDateCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime? {
        return localDataSource.getDueDateCompletionTime(routineId, dueDateNumber)
    }
}