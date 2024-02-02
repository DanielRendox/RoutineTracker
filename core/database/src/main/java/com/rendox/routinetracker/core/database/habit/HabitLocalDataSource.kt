package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.model.Habit

interface HabitLocalDataSource {
    suspend fun insertHabit(habit: Habit)
    suspend fun getHabitById(habitId: Long): Habit
    suspend fun getAllHabits(): List<Habit>
    suspend fun deleteHabitById(habitId: Long)
}