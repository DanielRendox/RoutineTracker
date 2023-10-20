package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine

class RoutineRepositoryImpl(
    private val localDataSource: RoutineLocalDataSource,
) : RoutineRepository {

    override suspend fun getRoutineById(id: Long): Routine {
        return localDataSource.getRoutineById(routineId = id)
    }

    override suspend fun insertRoutine(routine: Routine) {
        localDataSource.insertRoutine(routine)
    }

    override suspend fun setScheduleDeviation(newValue: Int) {
        TODO("Not yet implemented")
    }
}