package com.rendox.routinetracker.core.database.completion_history

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface CompletionHistoryLocalDataSource {

    suspend fun getRecordsInPeriod(
        habit: Habit,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord>

    suspend fun getAllRecords(): Map<Long, List<Habit.CompletionRecord>>

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