package com.rendox.routinetracker.core.data.habit

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

interface HabitRepository {

    suspend fun getHabitById(id: Long): Habit

    suspend fun insertHabit(habit: Habit)

    suspend fun getAllHabits(): List<Habit>

    suspend fun updateDueDateSpecificCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    )

    suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime?
}