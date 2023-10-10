package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class RoutineRepositoryImpl(
    private val localDataSource: RoutineLocalDataSource,
) : RoutineRepository {

    override fun getRoutineById(id: Long): Flow<Routine?> {
        return localDataSource.getRoutineById(id = id)
    }

    override suspend fun insertRoutine(routine: Routine) {
        localDataSource.insertRoutine(routine)
    }
}