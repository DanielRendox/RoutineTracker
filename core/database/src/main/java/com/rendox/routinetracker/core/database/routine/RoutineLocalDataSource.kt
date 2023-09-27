package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.RoutineType
import kotlinx.datetime.LocalDate

interface RoutineLocalDataSource {
    suspend fun getRoutineById(id: Long): RoutineEntity?

    suspend fun insertRoutine(
        id: Long? = null,
        type: RoutineType,
        name: String,
        startDate: LocalDate,
        backlogEnabled: Boolean,
        periodSeparation: Boolean,
    )
}