package com.rendox.routinetracker.core.data.streaks

import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak

class StreakRepositoryImpl(
    private val localDataSource: StreakLocalDataSource,
) : StreakRepository {

    override suspend fun upsertStreaks(
        habitId: Long,
        period: LocalDateRange,
        streaks: List<Streak>,
    ) = localDataSource.upsertStreaks(habitId, period, streaks)

    override suspend fun getLastStreak(habitId: Long): Streak? = localDataSource.getLastStreak(habitId)
    override suspend fun getLongestStreaks(habitId: Long): List<Streak> = localDataSource.getLongestStreaks(habitId)
}