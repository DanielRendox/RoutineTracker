package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

interface RoutineRepository {

    suspend fun getRoutineById(id: Long): Habit

    suspend fun insertRoutine(habit: Habit)

    suspend fun getAllRoutines(): List<Habit>

    suspend fun updateDueDateSpecificCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    )

    suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime?
}