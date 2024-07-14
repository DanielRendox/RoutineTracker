package com.rendox.routinetracker.core.data.completionhistory

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface CompletionHistoryRepository {

    suspend fun getRecordsInPeriod(
        habit: Habit,
        period: LocalDateRange,
    ): List<Habit.CompletionRecord>

    suspend fun getMultiHabitRecords(
        habitsToPeriods: List<Pair<List<Habit>, LocalDateRange>>,
    ): Map<Long, List<Habit.CompletionRecord>>

    suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    )

    suspend fun insertCompletions(completions: Map<Long, List<Habit.CompletionRecord>>)

    suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    )
}