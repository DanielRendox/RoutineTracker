package com.rendox.routinetracker.core.database.completiontime

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.schedule.GetCompletionTime
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime

class DueDateSpecificCompletionTimeLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : DueDateSpecificCompletionTimeLocalDataSource {
    override suspend fun getDueDateSpecificCompletionTime(
        scheduleId: Long,
        dueDateNumber: Int,
    ): LocalTime? = withContext(ioDispatcher) {
        db.dueDateEntityQueries
            .getCompletionTime(scheduleId, dueDateNumber)
            .executeAsOneOrNull()
            ?.toExternalModel()
    }

    override suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime,
        scheduleId: Long,
        dueDateNumber: Int,
    ) = withContext(ioDispatcher) {
        db.dueDateEntityQueries.updateCompletionTime(
            completionTimeHour = newTime.hour,
            completionTimeMinute = newTime.minute,
            scheduleId = scheduleId,
            dueDateNumber = dueDateNumber,
        )
    }

    private fun GetCompletionTime.toExternalModel(): LocalTime? =
        if (completionTimeHour != null && completionTimeMinute != null) {
            LocalTime(hour = completionTimeHour, minute = completionTimeMinute)
        } else {
            null
        }
}