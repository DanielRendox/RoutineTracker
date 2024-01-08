package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface CompletionHistoryRepository {
    suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Double

    suspend fun getRecordByDate(habitId: Long, date: LocalDate): Habit.CompletionRecord?
    suspend fun getLastCompletedRecord(habitId: Long): Habit.CompletionRecord?

    suspend fun getRecordsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord>

    suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    )

    suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    )
}