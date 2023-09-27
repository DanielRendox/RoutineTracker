package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate

interface RoutineRepository {
    suspend fun getRoutineById(id: Long): Routine?

    suspend fun insertRoutine(
        id: Long? = null,
        type: com.rendox.routinetracker.core.model.RoutineType,
        name: String,
        startDate: LocalDate,
        backlogEnabled: Boolean,
        periodSeparation: Boolean,
    )
}