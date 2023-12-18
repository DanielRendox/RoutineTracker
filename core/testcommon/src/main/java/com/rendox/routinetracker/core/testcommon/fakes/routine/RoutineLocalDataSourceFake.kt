package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

class RoutineLocalDataSourceFake(
    private val routineData: RoutineData
) : RoutineLocalDataSource {

    override suspend fun getRoutineById(routineId: Long): Habit =
        routineData.listOfHabits[(routineId - 1).toInt()]

    override suspend fun insertRoutine(habit: Habit) {
        routineData.listOfHabits =
            routineData.listOfHabits.toMutableList().apply { add(habit) }
    }

    override suspend fun getAllRoutines(): List<Habit> =
        routineData.listOfHabits

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