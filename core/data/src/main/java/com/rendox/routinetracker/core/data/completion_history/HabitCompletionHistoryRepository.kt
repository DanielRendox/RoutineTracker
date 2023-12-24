package com.rendox.routinetracker.core.data.completion_history

import kotlinx.datetime.LocalDate

interface HabitCompletionHistoryRepository {
    suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Double

    suspend fun getNumOfTimesCompletedOnDate(
        habitId: Long,
        date: LocalDate,
    ): Float?

    suspend fun getLastCompletedDate(habitId: Long): LocalDate?

    suspend fun insertCompletion(
        habitId: Long,
        date: LocalDate,
        numOfTimesCompleted: Float = 1F,
    )
}