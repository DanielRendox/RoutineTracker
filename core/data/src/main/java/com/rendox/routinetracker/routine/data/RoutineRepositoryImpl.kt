package com.rendox.routinetracker.routine.data

import com.rendox.performancetracker.feature.routine.Routine
import com.rendox.routinetracker.routine.model.RoutineType
import kotlinx.datetime.LocalDate

class RoutineRepositoryImpl(
    private val localDataSource: RoutineLocalDataSource,
): RoutineRepository {

    override suspend fun getRoutineById(id: Long): Routine? {
        return localDataSource.getRoutineById(id = id)
    }

    override suspend fun insertRoutine(
        id: Long?,
        type: RoutineType,
        name: String,
        startDate: LocalDate,
        backlogEnabled: Boolean,
        periodSeparation: Boolean,
    ) {
        localDataSource.insertRoutine(
            id = id,
            type = type,
            name = name,
            startDate = startDate,
            backlogEnabled = backlogEnabled,
            periodSeparation = periodSeparation,
        )
    }
}