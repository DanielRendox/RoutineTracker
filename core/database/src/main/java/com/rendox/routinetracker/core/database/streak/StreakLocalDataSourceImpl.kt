package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.toExternalModel
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext

class StreakLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : StreakLocalDataSource {
    override suspend fun insertStreaks(streaks: Map<Long, List<Streak>>) = withContext(ioDispatcher) {
        db.cachedStreakEntityQueries.transaction {
            for ((habitId, streakList) in streaks) {
                for (streak in streakList) {
                    db.cachedStreakEntityQueries.insertStreak(
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
        db.cachedStreakEntityQueries.transaction {
            db.cachedStreakEntityQueries.deleteStreaksInPeriod(
                habitId = habitId,
                periodStart = period.start,
                periodEnd = period.endInclusive,
            )
            for (streak in streaks) {
                db.cachedStreakEntityQueries.insertStreak(
                    habitId = habitId,
                    startDate = streak.startDate,
                    endDate = streak.endDate,
                )
            }
        }
    }

    override suspend fun getLastStreak(habitId: Long): Streak? = withContext(ioDispatcher) {
        db.cachedStreakEntityQueries.getLastStreak(habitId).executeAsOneOrNull()?.toExternalModel()
    }

    override suspend fun getLongestStreaks(habitId: Long): List<Streak> = withContext(ioDispatcher) {
        db.cachedStreakEntityQueries.getLongestStreak(habitId).executeAsList().map { it.toExternalModel() }
    }
}