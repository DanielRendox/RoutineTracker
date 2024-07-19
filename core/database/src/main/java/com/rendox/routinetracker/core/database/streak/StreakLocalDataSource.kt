package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

interface StreakLocalDataSource {

    suspend fun insertStreaks(streaks: Map<Long, List<Streak>>)

    suspend fun upsertStreaks(
        habitId: Long,
        period: LocalDateRange,
        streaks: List<Streak>,
    )

    suspend fun getAllStreaks(habitId: Long): List<Streak>

    suspend fun getStreaksInPeriod(
        habitId: Long,
        period: LocalDateRange,
    ): List<Streak>

    suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange>

    suspend fun getCashedPeriod(
        habitId: Long,
        dateInPeriod: LocalDate,
    ): LocalDateRange?

    suspend fun getLastStreak(habitId: Long): Streak?
    suspend fun getLongestStreaks(habitId: Long): List<Streak>
}