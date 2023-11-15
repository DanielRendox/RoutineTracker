package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineLocalDataSource {

    suspend fun getRoutineById(routineId: Long): Routine

    suspend fun insertRoutine(routine: Routine)

    suspend fun getAllRoutines(): List<Routine>
}