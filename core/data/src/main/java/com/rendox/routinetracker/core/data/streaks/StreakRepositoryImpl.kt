package com.rendox.routinetracker.core.data.streaks

import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class StreakRepositoryImpl(
    private val localDataSource: StreakLocalDataSource,
) : StreakRepository {
    override suspend fun insertStreaks(streaks: Map<Long, List<Streak>>) = localDataSource.insertStreaks(streaks)

    override suspend fun upsertStreaks(
        habitId: Long,
        period: LocalDateRange,
        streaks: List<Streak>,
    ) = localDataSource.upsertStreaks(habitId, period, streaks)

    override suspend fun getStreaksInPeriod(
        habitId: Long,
        period: LocalDateRange,
    ): List<Streak> = localDataSource.getStreaksInPeriod(habitId, period)

    override suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange> =
        localDataSource.getAllCashedPeriods(habitId)

    override suspend fun getCashedPeriod(
        habitId: Long,
        dateInPeriod: LocalDate,
    ): LocalDateRange? = localDataSource.getCashedPeriod(habitId, dateInPeriod)

    override suspend fun getLastStreak(habitId: Long): Streak? = localDataSource.getLastStreak(habitId)
    override suspend fun getLongestStreaks(habitId: Long): List<Streak> = localDataSource.getLongestStreaks(habitId)
}