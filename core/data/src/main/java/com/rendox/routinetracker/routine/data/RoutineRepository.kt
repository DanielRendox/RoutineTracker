package com.rendox.routinetracker.routine.data

import com.rendox.performancetracker.feature.routine.Routine
import com.rendox.routinetracker.routine.model.RoutineType
import kotlinx.datetime.LocalDate

interface RoutineRepository {
    suspend fun getRoutineById(id: Long): Routine?

    suspend fun insertRoutine(
        id: Long? = null,
        type: RoutineType,
        name: String,
        startDate: LocalDate,
        backlogEnabled: Boolean,
        periodSeparation: Boolean,
    )
}