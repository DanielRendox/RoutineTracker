package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface HabitLocalDataSource {
    suspend fun insertHabit(habit: Habit)
    suspend fun insertHabits(habits: List<Habit>)
    suspend fun getHabitById(habitId: Long): Habit
    suspend fun getAllHabits(): List<Habit>
    suspend fun getAllOngoingHabits(currentDate: LocalDate): List<Habit>
    suspend fun checkIfIsEmpty(): Boolean
    suspend fun deleteHabitById(habitId: Long)
}