package com.rendox.routinetracker.core.data.streaks

import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class StreakRepositoryImpl(
    private val localDataSource: StreakLocalDataSource,
) : StreakRepository {
    override suspend fun insertStreaks(
        streaks: List<Pair<Long, Streak>>,
        periods: List<Pair<Long, LocalDateRange>>
    ) = localDataSource.insertStreaks(streaks, periods)

    override suspend fun getAllStreaks(habitId: Long): List<Streak> =
        localDataSource.getAllStreaks(habitId)

    override suspend fun getStreaksInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate
    ): List<Streak> = localDataSource.getStreaksInPeriod(
        habitId, minDate, maxDate
    )

    override suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange> =
        localDataSource.getAllCashedPeriods(habitId)

    override suspend fun getCashedPeriod(
        habitId: Long, dateInPeriod: LocalDate
    ): LocalDateRange? = localDataSource.getCashedPeriod(habitId, dateInPeriod)

    override suspend fun deleteStreaksInPeriod(
        habitId: Long,
        periodStartDate: LocalDate,
        periodEndDate: LocalDate,
    ) = localDataSource.deleteStreaksInPeriod(
        habitId, periodStartDate, periodEndDate
    )
}