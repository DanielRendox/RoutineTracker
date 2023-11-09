package com.rendox.routinetracker.core.database.completion_time

import com.rendox.routinetracker.core.database.GetCompletionTime
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class CompletionTimeLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : CompletionTimeLocalDataSource {

    override suspend fun getCompletionTime(routineId: Long, date: LocalDate): LocalTime? {
        return withContext(dispatcher) {
            db.specificDateCustomCompletionTimeQueries.getCompletionTime(
                routineId, date
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun updateCompletionTime(routineId: Long, date: LocalDate, time: LocalTime) {
        withContext(dispatcher) {
            db.specificDateCustomCompletionTimeQueries.updateCompletionTime(
                time.hour, time.minute, routineId, date
            )
        }
    }

    override suspend fun insertCompletionTime(id: Long?, routineId: Long, date: LocalDate, time: LocalTime) {
        withContext(dispatcher) {
            db.specificDateCustomCompletionTimeQueries.insertCompletiontime(
                id, routineId, date, time.hour, time.minute
            )
        }
    }

    override suspend fun deleteCompletionTime(routineId: Long, date: LocalDate) {
        withContext(dispatcher) {
            db.specificDateCustomCompletionTimeQueries.deleteCompletionTime(
                routineId, date
            )
        }
    }

    private fun GetCompletionTime.toExternalModel() =
        LocalTime(hour = completionTimeHour, minute = completionTimeMinute)
}