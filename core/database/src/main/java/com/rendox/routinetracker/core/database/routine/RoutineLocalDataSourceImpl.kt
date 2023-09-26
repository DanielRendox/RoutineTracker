package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.model.RoutineType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class RoutineLocalDataSourceImpl(
    db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
): RoutineLocalDataSource {

    private val queries = db.routineQueries

    override suspend fun getRoutineById(id: Long): Routine? {
        return withContext(dispatcher) {
            queries.getRoutineById(id).executeAsOneOrNull()
        }
    }

    override suspend fun insertRoutine(
        id: Long?,
        type: RoutineType,
        name: String,
        startDate: LocalDate,
        backlogEnabled: Boolean,
        periodSeparation: Boolean,
    ) {
        return withContext(dispatcher) {
            queries.insertRoutine(
                id = id,
                type = type,
                name = name,
                startDate = startDate,
                backlogEnabled = backlogEnabled,
                periodSeparation = periodSeparation,
                vacationEndDate = null,
                vacationStartDate = null,
            )
        }
    }
}