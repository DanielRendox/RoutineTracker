package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.RoutineType
import kotlinx.datetime.LocalDate

interface RoutineLocalDataSource {

    suspend fun getRoutineById(id: Long): Routine?

    suspend fun insertRoutine(routine: Routine)
}