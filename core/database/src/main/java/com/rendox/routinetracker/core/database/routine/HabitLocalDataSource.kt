package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

interface HabitLocalDataSource {

    suspend fun getHabitById(routineId: Long): Habit

    suspend fun insertHabit(habit: Habit)

    suspend fun getAllHabits(): List<Habit>

    suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime, routineId: Long, dueDateNumber: Int
    )

    suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime?
}