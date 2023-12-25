package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.model.Habit
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class CompletionHistoryRepositoryFake(
    private val habitData: HabitData
) : CompletionHistoryRepository {
    override suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ): Double {
        return habitData.completionHistory.value.filter {
            it.first == habitId
                    && (minDate == null || minDate <= it.second.date)
                    && (maxDate == null || it.second.date <= maxDate)
        }.sumOf { it.second.numOfTimesCompleted.toDouble() }
    }

    override suspend fun getRecordByDate(habitId: Long, date: LocalDate): Habit.CompletionRecord? {
        return habitData.completionHistory.value
            .find { it.first == habitId && it.second.date == date }?.second
    }

    override suspend fun getLastCompletedRecord(habitId: Long): Habit.CompletionRecord? {
        return habitData.completionHistory.value
            .filter { it.first == habitId }
            .maxByOrNull { it.second.date }?.second
    }

    override suspend fun insertCompletion(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    ) {
        habitData.completionHistory.update {
            it.toMutableList().apply { add(habitId to completionRecord) }
        }
    }

    override suspend fun deleteCompletionByDate(habitId: Long, date: LocalDate) {
        habitData.completionHistory.update { completionHistory ->
            val completionIndex = completionHistory.indexOfFirst {
                it.first == habitId && it.second.date == date
            }
            if (completionIndex != -1) {
                completionHistory.toMutableList().apply { removeAt(completionIndex) }
            } else {
                completionHistory
            }
        }
    }
}