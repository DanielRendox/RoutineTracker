package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalTime

interface RoutineRepository {

    suspend fun getRoutineById(id: Long): Routine

    suspend fun insertRoutine(routine: Routine)

    suspend fun getAllRoutines(): List<Routine>

    suspend fun updateDueDateSpecificCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    )

    suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime?
}