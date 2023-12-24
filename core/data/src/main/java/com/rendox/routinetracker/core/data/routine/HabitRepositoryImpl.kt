package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.HabitLocalDataSource
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalTime

class HabitRepositoryImpl(
    private val localDataSource: HabitLocalDataSource,
) : HabitRepository {

    override suspend fun getHabitById(id: Long): Habit {
        return localDataSource.getHabitById(habitId = id)
    }

    override suspend fun insertHabit(habit: Habit) {
        localDataSource.insertHabit(habit)
    }

    override suspend fun getAllHabits(): List<Habit> {
        return localDataSource.getAllHabits()
    }

    override suspend fun updateDueDateSpecificCompletionTime(
        time: LocalTime, routineId: Long, dueDateNumber: Int
    ) {
        localDataSource.updateDueDateSpecificCompletionTime(
            time, routineId, dueDateNumber
        )
    }

    override suspend fun getDueDateSpecificCompletionTime(
        routineId: Long, dueDateNumber: Int
    ): LocalTime? {
        return localDataSource.getDueDateSpecificCompletionTime(
            routineId, dueDateNumber
        )
    }
}