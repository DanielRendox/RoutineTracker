package com.rendox.routinetracker.core.database.completiontime

import com.rendox.routinetracker.core.database.GetCompletionTime
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class CompletionTimeLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : CompletionTimeLocalDataSource {

    override suspend fun getCompletionTime(
        routineId: Long,
        date: LocalDate,
    ): LocalTime? = withContext(ioDispatcher) {
        db.specificDateCustomCompletionTimeQueries.getCompletionTime(
            routineId,
            date,
        ).executeAsOneOrNull()?.toExternalModel()
    }

    override suspend fun updateCompletionTime(
        routineId: Long,
        date: LocalDate,
        time: LocalTime,
    ) {
        withContext(ioDispatcher) {
            db.specificDateCustomCompletionTimeQueries.updateCompletionTime(
                time.hour,
                time.minute,
                routineId,
                date,
            )
        }
    }

    override suspend fun insertCompletionTime(
        id: Long?,
        routineId: Long,
        date: LocalDate,
        time: LocalTime,
    ) {
        withContext(ioDispatcher) {
            db.specificDateCustomCompletionTimeQueries.insertCompletiontime(
                routineId = routineId,
                date = date,
                completionTimeHour = time.hour,
                completionTimeMinute = time.minute,
            )
        }
    }

    override suspend fun deleteCompletionTime(
        routineId: Long,
        date: LocalDate,
    ) {
        withContext(ioDispatcher) {
            db.specificDateCustomCompletionTimeQueries.deleteCompletionTime(
                routineId,
                date,
            )
        }
    }

    private fun GetCompletionTime.toExternalModel() =
        LocalTime(hour = completionTimeHour, minute = completionTimeMinute)
}