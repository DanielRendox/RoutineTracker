package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

interface HabitLocalDataSource {

    suspend fun getHabitById(habitId: Long): Habit

    suspend fun insertHabit(habit: Habit)

    suspend fun getAllHabits(): List<Habit>

    suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime, habitId: Long, dueDateNumber: Int
    )

    suspend fun getDueDateSpecificCompletionTime(
        habitId: Long, dueDateNumber: Int
    ): LocalTime?
}