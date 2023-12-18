package com.rendox.routinetracker.core.database.routine

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

interface RoutineLocalDataSource {

    suspend fun getRoutineById(routineId: Long): Habit

    suspend fun insertRoutine(habit: Habit)

    suspend fun getAllRoutines(): List<Habit>

    suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime, routineId: Long, dueDateNumber: Int
    )

    suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime?
}