package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate

class RoutineRepositoryImpl(
    private val localDataSource: RoutineLocalDataSource,
) : RoutineRepository {

    override suspend fun getRoutineById(id: Long): Routine? {
        return localDataSource.getRoutineById(id = id)
    }

    override suspend fun insertRoutine(routine: Routine) {
        localDataSource.insertRoutine(routine)
    }
}