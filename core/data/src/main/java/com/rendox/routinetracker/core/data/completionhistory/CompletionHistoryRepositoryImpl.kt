package com.rendox.routinetracker.core.data.completionhistory

import com.rendox.routinetracker.core.database.completionhistory.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class CompletionHistoryRepositoryImpl(
    private val localDataSource: CompletionHistoryLocalDataSource,
) : CompletionHistoryRepository {

    override suspend fun getRecordsInPeriod(
        habit: Habit,
        period: LocalDateRange,
    ): List<Habit.CompletionRecord> = localDataSource.getRecordsInPeriod(habit, period)

    override suspend fun getMultiHabitRecords(
        habitsToPeriods: List<Pair<List<Habit>, LocalDateRange>>,
    ): Map<Long, List<Habit.CompletionRecord>> = localDataSource.getMultiHabitRecords(habitsToPeriods)

    override suspend fun getRecordsWithoutStreaks(habit: Habit): List<Habit.CompletionRecord> =
        localDataSource.getRecordsWithoutStreaks(habit)

    override suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    ) = localDataSource.insertCompletion(habitId, completionRecord)

    override suspend fun insertCompletions(completions: Map<Long, List<Habit.CompletionRecord>>) =
        localDataSource.insertCompletions(completions)

    override suspend fun insertCompletionAndCacheStreaks(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
        period: LocalDateRange,
        streaks: List<Streak>,
    ) = localDataSource.insertCompletionAndCacheStreaks(
        habitId,
        completionRecord,
        period,
        streaks,
    )

    override suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    ) = localDataSource.deleteCompletionByDate(habitId, date)
}