package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalTime

class HabitRepositoryFake(
    private val habitData: HabitData
) : HabitRepository {

    override suspend fun getHabitById(id: Long): Habit =
        habitData.listOfHabits.value[(id - 1).toInt()]

    override suspend fun insertHabit(habit: Habit) {
        habitData.listOfHabits.update {
            it.toMutableList().apply { add(habit) }
        }
    }

    override suspend fun getAllHabits(): List<Habit> = habitData.listOfHabits.value

    override suspend fun updateDueDateSpecificCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    ) {
        habitData.dueDateCompletionTimes.update { dueDateCompletionTimes ->
            val existingCompletionTime = dueDateCompletionTimes.find {
                it.routineId == routineId && it.dueDateNumber == dueDateNumber
            }
            val newValue = DueDateCompletionTimeEntity(routineId, dueDateNumber, time)
            if (existingCompletionTime == null) {
                dueDateCompletionTimes.toMutableList().apply { add(newValue) }
            } else {
                val existingCompletionTimeIndex =
                    dueDateCompletionTimes.indexOf(existingCompletionTime)
                dueDateCompletionTimes.toMutableList()
                    .apply { set(existingCompletionTimeIndex, newValue) }
            }
        }
    }

    override suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime? {
        return habitData.dueDateCompletionTimes.value.find {
            it.routineId == routineId && it.dueDateNumber == dueDateNumber
        }?.completionTime
    }
}