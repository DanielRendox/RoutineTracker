package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

class CompletionHistoryRepositoryImpl(
    private val localDataSource: CompletionHistoryLocalDataSource
): CompletionHistoryRepository {
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

    override suspend fun getLastCompletedRecord(habitId: Long): Habit.CompletionRecord? {
        return localDataSource.getLastCompletedRecord(habitId)
    }

    override suspend fun insertCompletion(habitId: Long, completionRecord: Habit.CompletionRecord) {
        localDataSource.insertCompletion(habitId, completionRecord)
    }

    override suspend fun deleteCompletionByDate(habitId: Long, date: LocalDate) {
        localDataSource.deleteCompletionByDate(habitId, date)
    }
}