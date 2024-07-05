package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.database.CachedStreakEntity
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class StreakLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : StreakLocalDataSource {
    override suspend fun insertStreaks(
        streaks: List<Pair<Long, Streak>>,
        periods: List<Pair<Long, LocalDateRange>>,
    ) = withContext(ioDispatcher) {
        db.cachedStreakEntityQueries.transaction {
            for (streak in streaks) {
                db.cachedStreakEntityQueries.insertStreak(
                    habitId = streak.first,
                    startDate = streak.second.startDate,
                    endDate = streak.second.endDate,
                )
            }
        }
    }

    override suspend fun getAllStreaks(habitId: Long): List<Streak> = withContext(ioDispatcher) {
        db.cachedStreakEntityQueries
            .getAllStreaks(habitId)
            .executeAsList()
            .map { it.toExternalModel() }
    }

    override suspend fun getStreaksInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Streak> = withContext(ioDispatcher) {
        db.cachedStreakEntityQueries
            .getStreaksInPeriod(
                habitId = habitId,
                periodStart = minDate,
                periodEnd = maxDate,
            ).executeAsList()
            .map { it.toExternalModel() }
    }

    override suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange> = TODO()

    override suspend fun getCashedPeriod(
        habitId: Long,
        dateInPeriod: LocalDate,
    ): LocalDateRange = TODO()

    override suspend fun deleteStreaksInPeriod(
        habitId: Long,
        periodStartDate: LocalDate,
        periodEndDate: LocalDate,
    ) = withContext(ioDispatcher) {
        db.cachedStreakEntityQueries.deleteStreaksInPeriod(
            habitId = habitId,
            periodStart = periodStartDate,
            periodEnd = periodEndDate,
        )
    }

    private fun CachedStreakEntity.toExternalModel() = Streak(
        startDate = startDate,
        endDate = endDate,
    )
}