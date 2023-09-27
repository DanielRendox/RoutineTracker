package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate

class RoutineRepositoryImpl(
    private val localDataSource: RoutineLocalDataSource,
): RoutineRepository {

    override suspend fun getRoutineById(id: Long): Routine? {
        return localDataSource.getRoutineById(id = id)?.asExternalModel()
    }

    override suspend fun insertRoutine(
        id: Long?,
        type: com.rendox.routinetracker.core.model.RoutineType,
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