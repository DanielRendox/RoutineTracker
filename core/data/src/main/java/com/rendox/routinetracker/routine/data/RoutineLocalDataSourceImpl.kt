package com.rendox.routinetracker.routine.data

import com.rendox.performancetracker.Database
import com.rendox.performancetracker.feature.routine.Routine
import com.rendox.routinetracker.routine.model.RoutineType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class RoutineLocalDataSourceImpl(
    db: Database,
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