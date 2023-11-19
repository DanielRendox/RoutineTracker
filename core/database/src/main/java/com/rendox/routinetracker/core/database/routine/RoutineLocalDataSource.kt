package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalTime

interface RoutineLocalDataSource {

    suspend fun getRoutineById(routineId: Long): Routine

    suspend fun insertRoutine(routine: Routine)

    suspend fun getAllRoutines(): List<Routine>

    suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime, routineId: Long, dueDateNumber: Int
    )

    suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime?
}