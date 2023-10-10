package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineLocalDataSource {

    fun getRoutineById(id: Long): Flow<Routine?>

    suspend fun insertRoutine(routine: Routine)
}