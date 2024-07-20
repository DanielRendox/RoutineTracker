package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.toExternalModel
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class StreakLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : StreakLocalDataSource {
    override suspend fun insertStreaks(streaks: Map<Long, List<Streak>>) = withContext(ioDispatcher) {
        db.cashedStreakQueries.transaction {
            for ((habitId, streakList) in streaks) {
                for (streak in streakList) {
                    db.cashedStreakQueries.insertStreak(
                        habitId = habitId,
                        startDate = streak.startDate,
                        endDate = streak.endDate,
                    )
                }
            }
        }
    }

    override suspend fun upsertStreaks(
        habitId: Long,
        period: LocalDateRange,
        streaks: List<Streak>,
    ) {
        db.cashedStreakQueries.transaction {
            db.cashedStreakQueries.deleteStreaksInPeriod(
                habitId = habitId,
                periodStart = period.start,
                periodEnd = period.endInclusive,
            )
            for (streak in streaks) {
                db.cashedStreakQueries.insertStreak(
                    habitId = habitId,
                    startDate = streak.startDate,
                    endDate = streak.endDate,
                )
            }
        }
    }

    override suspend fun getStreaksInPeriod(
        habitId: Long,
        period: LocalDateRange,
    ): List<Streak> = withContext(ioDispatcher) {
        db.cashedStreakQueries
            .getStreaksInPeriod(
                habitId = habitId,
                periodStart = period.start,
                periodEnd = period.endInclusive,
            ).executeAsList()
            .map { it.toExternalModel() }
    }

    override suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange> = withContext(ioDispatcher) {
        db.cashedStreakQueries
            .getAllCashedPeriods(habitId)
            .executeAsList()
            .map { it.toExternalModel() }
    }

    override suspend fun getCashedPeriod(
        habitId: Long,
        dateInPeriod: LocalDate,
    ): LocalDateRange? = withContext(ioDispatcher) {
        db.cashedStreakQueries
            .getCashedPeriod(
                habitId = habitId,
                dateInPeriod = dateInPeriod,
            ).executeAsOneOrNull()
            ?.toExternalModel()
    }

    override suspend fun getLastStreak(habitId: Long): Streak? = withContext(ioDispatcher) {
        db.cashedStreakQueries.getLastStreak(habitId).executeAsOneOrNull()?.toExternalModel()
    }

    override suspend fun getLongestStreaks(habitId: Long): List<Streak> = withContext(ioDispatcher) {
        db.cashedStreakQueries.getLongestStreak(habitId).executeAsList().map { it.toExternalModel() }
    }
}