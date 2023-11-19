package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalTime

class RoutineLocalDataSourceFake(
    private val routineData: RoutineData
) : RoutineLocalDataSource {

    override suspend fun getRoutineById(routineId: Long): Routine =
        routineData.listOfRoutines[(routineId - 1).toInt()]

    override suspend fun insertRoutine(routine: Routine) {
        routineData.listOfRoutines =
            routineData.listOfRoutines.toMutableList().apply { add(routine) }
    }

    override suspend fun getAllRoutines(): List<Routine> =
        routineData.listOfRoutines

    override suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime, routineId: Long, dueDateNumber: Int
    ) {
        val existingCompletionTime = routineData.dueDateCompletionTimes.find {
            it.routineId == routineId && it.dueDateNumber == dueDateNumber
        }
        val newValue = DueDateCompletionTimeEntity(routineId, dueDateNumber, newTime)
        if (existingCompletionTime == null) {
            routineData.dueDateCompletionTimes =
                routineData.dueDateCompletionTimes.toMutableList().apply { add(newValue) }
        } else {
            val existingCompletionTimeIndex =
                routineData.dueDateCompletionTimes.indexOf(existingCompletionTime)
            routineData.dueDateCompletionTimes =
                routineData.dueDateCompletionTimes.toMutableList().apply {
                    set(existingCompletionTimeIndex, newValue)
                }
        }
    }

    override suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime? {
        return routineData.dueDateCompletionTimes.find {
            it.routineId == routineId && it.dueDateNumber == dueDateNumber
        }?.completionTime
    }
}