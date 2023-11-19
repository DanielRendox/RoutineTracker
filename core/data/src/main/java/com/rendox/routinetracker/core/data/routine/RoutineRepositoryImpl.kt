package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalTime

class RoutineRepositoryImpl(
    private val localDataSource: RoutineLocalDataSource,
) : RoutineRepository {

    override suspend fun getRoutineById(id: Long): Routine {
        return localDataSource.getRoutineById(routineId = id)
    }

    override suspend fun insertRoutine(routine: Routine) {
        localDataSource.insertRoutine(routine)
    }

    override suspend fun getAllRoutines(): List<Routine> {
        return localDataSource.getAllRoutines()
    }

    override suspend fun updateDueDateSpecificCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    ) {
        localDataSource.updateDueDateSpecificCompletionTime(
            time, routineId, dueDateNumber
        )
    }

    override suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime? {
        return localDataSource.getDueDateSpecificCompletionTime(
            routineId, dueDateNumber
        )
    }
}