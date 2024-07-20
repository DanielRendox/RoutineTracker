package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak

interface StreakLocalDataSource {

    suspend fun insertStreaks(streaks: Map<Long, List<Streak>>)

    suspend fun upsertStreaks(
        habitId: Long,
        period: LocalDateRange,
        streaks: List<Streak>,
    )

    suspend fun getLastStreak(habitId: Long): Streak?
    suspend fun getLongestStreaks(habitId: Long): List<Streak>
}