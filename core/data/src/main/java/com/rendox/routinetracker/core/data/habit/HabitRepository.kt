package com.rendox.routinetracker.core.data.habit

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface HabitRepository {

    suspend fun insertHabit(habit: Habit)
    suspend fun insertHabits(habits: List<Habit>)
    suspend fun getHabitById(id: Long): Habit

    /**
     * @return all habits that have already started and have not yet finished
     * at the moment of the specified [currentDate]
     */
    suspend fun getAllOngoingHabits(currentDate: LocalDate): List<Habit>
    suspend fun getAllHabits(): List<Habit>
    suspend fun deleteHabit(id: Long)
}