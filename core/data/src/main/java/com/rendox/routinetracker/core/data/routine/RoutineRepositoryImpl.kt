package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

class RoutineRepositoryImpl(
    private val localDataSource: RoutineLocalDataSource,
) : RoutineRepository {

    override suspend fun getRoutineById(id: Long): Habit {
        return localDataSource.getRoutineById(routineId = id)
    }

    override suspend fun insertRoutine(habit: Habit) {
        localDataSource.insertRoutine(habit)
    }

    override suspend fun getAllRoutines(): List<Habit> {
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