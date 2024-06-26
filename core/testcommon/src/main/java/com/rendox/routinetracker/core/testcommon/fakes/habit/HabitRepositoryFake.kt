package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.flow.update

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

    override suspend fun insertHabits(habits: List<Habit>) {
        habitData.listOfHabits.update {
            it.toMutableList().apply { addAll(habits) }
        }
    }

    override suspend fun getAllHabits(): List<Habit> = habitData.listOfHabits.value

    override suspend fun deleteHabit(id: Long) = habitData.listOfHabits.update {
        it.toMutableList().apply { removeAt((id - 1).toInt()) }
    }
}