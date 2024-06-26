package com.rendox.routinetracker.core.data.completionhistory

import com.rendox.routinetracker.core.database.completionhistory.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

class CompletionHistoryRepositoryImpl(
    private val localDataSource: CompletionHistoryLocalDataSource,
) : CompletionHistoryRepository {

    override suspend fun getRecordsInPeriod(
        habit: Habit,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord> = localDataSource.getRecordsInPeriod(
        habit,
        minDate,
        maxDate,
    )

    override suspend fun getMultiHabitRecords(
        habitsToPeriods: List<Pair<List<Habit>, LocalDateRange>>,
    ): Map<Long, List<Habit.CompletionRecord>> = localDataSource.getMultiHabitRecords(habitsToPeriods)

    override suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    ) {
        localDataSource.insertCompletion(habitId, completionRecord)
    }

    override suspend fun insertCompletions(completions: Map<Long, List<Habit.CompletionRecord>>) {
        localDataSource.insertCompletions(completions)
    }

    override suspend fun deleteCompletionByDate(
        habitId: Long,
        date: LocalDate,
    ) {
        localDataSource.deleteCompletionByDate(habitId, date)
    }
}