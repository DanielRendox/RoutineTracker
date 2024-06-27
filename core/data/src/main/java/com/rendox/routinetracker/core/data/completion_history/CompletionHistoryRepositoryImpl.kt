package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

class CompletionHistoryRepositoryImpl(
    private val localDataSource: CompletionHistoryLocalDataSource
) : CompletionHistoryRepository {
    override suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ): Double {
        return localDataSource.getNumOfTimesCompletedInPeriod(
            habitId, minDate, maxDate
        )
    }

    override suspend fun getRecordByDate(habitId: Long, date: LocalDate): Habit.CompletionRecord? {
        return localDataSource.getRecordByDate(
            habitId, date
        )
    }

    override suspend fun getLastCompletedRecord(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Habit.CompletionRecord? {
        return localDataSource.getLastCompletedRecord(habitId, minDate, maxDate)
    }

    override suspend fun getFirstCompletedRecord(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): Habit.CompletionRecord? {
        return localDataSource.getFirstCompletedRecord(habitId, minDate, maxDate)
    }

    override suspend fun getRecordsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Habit.CompletionRecord> {
        return localDataSource.getRecordsInPeriod(
            habitId, minDate, maxDate
        )
    }

    override suspend fun getAllRecords(): Map<Long, List<Habit.CompletionRecord>> {
        return localDataSource.getAllRecords()
    }

    override suspend fun insertCompletion(habitId: Long, completionRecord: Habit.CompletionRecord) {
        localDataSource.insertCompletion(habitId, completionRecord)
    }

    override suspend fun insertCompletions(
        completions: Map<Long, List<Habit.CompletionRecord>>
    ) {
        localDataSource.insertCompletions(completions)
    }

    override suspend fun deleteCompletionByDate(habitId: Long, date: LocalDate) {
        localDataSource.deleteCompletionByDate(habitId, date)
    }
}