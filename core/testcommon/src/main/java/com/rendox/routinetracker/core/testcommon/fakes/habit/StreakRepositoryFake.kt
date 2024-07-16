package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class StreakRepositoryFake(
    private val habitData: HabitData,
) : StreakRepository {

    override suspend fun insertStreaks(
        streaks: List<Pair<Long, Streak>>,
        periods: List<Pair<Long, LocalDateRange>>,
    ) {
        habitData.streaks.update {
            it.toMutableList().apply { addAll(streaks) }
        }
        habitData.streakCashedPeriods.update {
            it.toMutableList().apply { addAll(periods) }.distinct()
        }
    }

    override suspend fun getAllStreaks(habitId: Long): List<Streak> =
        habitData.streaks.value.filter { it.first == habitId }.map { it.second }

    override suspend fun getStreaksInPeriod(
        habitId: Long,
        period: LocalDateRange,
    ): List<Streak> = habitData.streaks.value.filter {
        it.first == habitId &&
            it.second.startDate <= period.endInclusive &&
            it.second.endDate >= period.start
    }.map { it.second }

    override suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange> =
        habitData.streakCashedPeriods.value.map { it.second }

    override suspend fun getCashedPeriod(
        habitId: Long,
        dateInPeriod: LocalDate,
    ): LocalDateRange? = habitData.streakCashedPeriods.value.find {
        it.first == habitId && it.second.contains(dateInPeriod)
    }?.second

    override suspend fun deleteStreaksInPeriod(
        habitId: Long,
        period: LocalDateRange,
    ) = habitData.streaks.update { streaks ->
        streaks.toMutableList().apply {
            removeAll {
                it.first == habitId && period.start <= it.second.startDate && it.second.endDate <= period.endInclusive
            }
        }
    }
}