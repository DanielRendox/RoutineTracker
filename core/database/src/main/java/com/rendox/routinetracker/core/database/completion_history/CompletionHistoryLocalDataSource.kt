package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface CompletionHistoryLocalDataSource {
    suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Double

    suspend fun getRecordByDate(habitId: Long, date: LocalDate): Habit.CompletionRecord?
    suspend fun getLastCompletedRecord(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Habit.CompletionRecord?

    suspend fun getFirstCompletedRecord(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Habit.CompletionRecord?

    suspend fun getRecordsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord>

    suspend fun getAllRecords(): List<Pair<Long, Habit.CompletionRecord>>

    suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    )

    suspend fun insertCompletions(
        completions: Map<Long, List<Habit.CompletionRecord>>
    )

    suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    )
}