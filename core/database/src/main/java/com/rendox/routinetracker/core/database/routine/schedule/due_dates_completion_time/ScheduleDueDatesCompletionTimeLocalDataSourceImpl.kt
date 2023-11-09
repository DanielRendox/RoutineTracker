package com.rendox.routinetracker.core.database.routine.schedule.due_dates_completion_time

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.schedule.GetCompletionTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime

class ScheduleDueDatesCompletionTimeLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : ScheduleDueDatesCompletionTimeLocalDataSource {
    override suspend fun updateDueDateCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    ) {
        withContext(dispatcher) {
            db.dueDateEntityQueries.updateCompletionTime(time.hour, time.minute, routineId, dueDateNumber)
        }
    }

    override suspend fun getDueDateCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime? {
        return withContext(dispatcher) {
            db.dueDateEntityQueries
                .getCompletionTime(routineId, dueDateNumber)
                .executeAsOneOrNull()
                ?.toExternalModel()
        }
    }

    private fun GetCompletionTime.toExternalModel(): LocalTime? {
        return if (completionTimeHour != null && completionTimeMinute != null) {
            LocalTime(hour = completionTimeHour, minute = completionTimeMinute)
        } else null
    }
}