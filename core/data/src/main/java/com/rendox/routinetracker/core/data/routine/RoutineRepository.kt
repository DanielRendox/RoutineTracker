package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.model.Routine

interface RoutineRepository {

    suspend fun getRoutineById(id: Long): Routine

    suspend fun insertRoutine(routine: Routine)

    suspend fun setScheduleDeviation(newValue: Int)
}