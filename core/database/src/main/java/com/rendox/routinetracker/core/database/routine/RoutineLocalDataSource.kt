package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Routine

interface RoutineLocalDataSource {

    suspend fun getRoutineById(id: Long): Routine?

    suspend fun insertRoutine(routine: Routine)
}