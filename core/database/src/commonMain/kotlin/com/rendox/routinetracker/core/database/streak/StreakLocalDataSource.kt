package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

interface StreakLocalDataSource {
    suspend fun insertStreaks(
        streaks: List<Pair<Long, Streak>>,
        periods: List<Pair<Long, LocalDateRange>>
    )

    suspend fun getAllStreaks(habitId: Long): List<Streak>

    suspend fun getStreaksInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Streak>

    suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange>

    suspend fun getCashedPeriod(
        habitId: Long, dateInPeriod: LocalDate
    ): LocalDateRange?

    suspend fun deleteStreaksInPeriod(
        habitId: Long,
        periodStartDate: LocalDate,
        periodEndDate: LocalDate,
    )
}