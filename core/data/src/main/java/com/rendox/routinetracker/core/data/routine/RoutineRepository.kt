package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.model.Routine
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface RoutineRepository {

    fun getRoutineById(id: Long): Flow<Routine?>

    suspend fun insertRoutine(routine: Routine)
}