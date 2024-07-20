package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.logic.getDurationInDays
import com.rendox.routinetracker.core.logic.isSubsetOf
import com.rendox.routinetracker.core.logic.joinAdjacentStreaks
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.flow.update

class StreakRepositoryFake(
    private val habitData: HabitData,
) : StreakRepository {

    fun insertStreaks(streaks: Map<Long, List<Streak>>) = habitData.streaks.update {
        it.toMutableList().apply {
            for ((habitId, streakList) in streaks) {
                addAll(streakList.map { streak -> habitId to streak })
            }
        }
    }

    override suspend fun upsertStreaks(
        habitId: Long,
        period: LocalDateRange,
        streaks: List<Streak>,
    ) = habitData.streaks.update { streakList ->
        streakList.toMutableList().apply {
            removeAll {
                it.first == habitId && it.second.isSubsetOf(period)
            }
            addAll(streaks.map { habitId to it })
        }
    }

    fun getAllStreaks(habitId: Long): List<Streak> =
        habitData.streaks.value.filter { it.first == habitId }.map { it.second }

    override suspend fun getLastStreak(habitId: Long): Streak? = habitData.streaks.value
        .filter { it.first == habitId }
        .map { it.second }
        .joinAdjacentStreaks()
        .maxByOrNull { it.startDate }

    override suspend fun getLongestStreaks(habitId: Long): List<Streak> {
        val streaks = habitData.streaks.value
            .filter { it.first == habitId }
            .map { it.second }
            .joinAdjacentStreaks()
        val longestStreakDuration = streaks.maxOfOrNull { it.getDurationInDays() }
        return streaks.filter { it.getDurationInDays() == longestStreakDuration }
    }
}