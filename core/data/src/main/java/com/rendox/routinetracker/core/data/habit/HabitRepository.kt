package com.rendox.routinetracker.core.data.habit

import com.rendox.routinetracker.core.model.Habit

interface HabitRepository {

    suspend fun insertHabit(habit: Habit)
    suspend fun getHabitById(id: Long): Habit
    suspend fun getAllHabits(): List<Habit>
    suspend fun deleteHabit(id: Long)
}