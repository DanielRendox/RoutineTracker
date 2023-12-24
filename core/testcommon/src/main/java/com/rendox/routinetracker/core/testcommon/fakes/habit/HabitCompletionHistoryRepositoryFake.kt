package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.completion_history.HabitCompletionHistoryRepository
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class HabitCompletionHistoryRepositoryFake(
    private val habitData: HabitData
) : HabitCompletionHistoryRepository {
    override suspend fun getNumOfTimesCompletedInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ): Double {
        return habitData.completionHistory.value.filter {
            it.first == habitId
                    && (minDate == null || minDate <= it.second)
                    && (maxDate == null || it.second <= maxDate)
        }.sumOf { it.third.toDouble() }
    }

    override suspend fun getNumOfTimesCompletedOnDate(habitId: Long, date: LocalDate): Float? {
        return habitData.completionHistory.value
            .find { it.first == habitId && it.second == date }
            ?.third
    }

    override suspend fun getLastCompletedDate(habitId: Long): LocalDate? {
        return habitData.completionHistory.value
            .filter { it.first == habitId }
            .maxByOrNull { it.second }?.second
    }

    override suspend fun insertCompletion(
        habitId: Long,
        date: LocalDate,
        numOfTimesCompleted: Float,
    ) {
        habitData.completionHistory.update {
            it.toMutableList().apply { add(Triple(habitId, date, numOfTimesCompleted)) }
        }
    }
}