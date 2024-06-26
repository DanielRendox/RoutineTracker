package com.rendox.routinetracker.core.data.habit

import com.rendox.routinetracker.core.database.habit.HabitLocalDataSource
import com.rendox.routinetracker.core.model.Habit

class HabitRepositoryImpl(
    private val localDataSource: HabitLocalDataSource,
) : HabitRepository {
    override suspend fun insertHabit(habit: Habit) {
        localDataSource.insertHabit(habit)
    }

    override suspend fun insertHabits(habits: List<Habit>) {
        localDataSource.insertHabits(habits)
    }

    override suspend fun getHabitById(id: Long): Habit {
        return localDataSource.getHabitById(habitId = id)
    }

    override suspend fun getAllHabits(): List<Habit> {
        return localDataSource.getAllHabits()
    }

    override suspend fun deleteHabit(id: Long) {
        localDataSource.deleteHabitById(id)
    }
}