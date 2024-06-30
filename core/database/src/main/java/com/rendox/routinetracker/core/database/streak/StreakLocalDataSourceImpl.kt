package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.database.CashedStreakEntity
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.StreakCashedPeriodEntity
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class StreakLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
) : StreakLocalDataSource {
    override suspend fun insertStreaks(streaks: List<Pair<Long, Streak>>, periods: List<Pair<Long, LocalDateRange>>) =
        withContext(ioDispatcher) {
            db.cashedStreakQueries.transaction {
                for (streak in streaks) {
                    db.cashedStreakQueries.insertStreak(
                        habitId = streak.first,
                        startDate = streak.second.startDate,
                        endDate = streak.second.endDate,
                    )
                }
                for (period in periods) {
                    db.cashedStreakQueries.insertPeriod(
                        habitId = period.first,
                        startDate = period.second.start,
                        endDate = period.second.endInclusive,
                    )
                }
            }
        }

    override suspend fun getAllStreaks(habitId: Long): List<Streak> =
        withContext(ioDispatcher) {
            db.cashedStreakQueries
                .getAllStreaks(habitId)
                .executeAsList()
                .map { it.toExternalModel() }
        }

    override suspend fun getStreaksInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Streak> =
        withContext(ioDispatcher) {
            db.cashedStreakQueries
                .getStreaksInPeriod(
                    habitId = habitId,
                    periodStart = minDate,
                    periodEnd = maxDate,
                ).executeAsList()
                .map { it.toExternalModel() }
        }

    override suspend fun getAllCashedPeriods(habitId: Long): List<LocalDateRange> =
        withContext(ioDispatcher) {
            db.cashedStreakQueries
                .getAllCashedPeriods(habitId)
                .executeAsList()
                .map { it.toExternalModel() }
        }

    override suspend fun getCashedPeriod(habitId: Long, dateInPeriod: LocalDate): LocalDateRange? =
        withContext(ioDispatcher) {
            db.cashedStreakQueries
                .getCashedPeriod(
                    habitId = habitId,
                    dateInPeriod = dateInPeriod,
                ).executeAsOneOrNull()
                ?.toExternalModel()
        }

    override suspend fun deleteStreaksInPeriod(
        habitId: Long,
        periodStartDate: LocalDate,
        periodEndDate: LocalDate,
    ) = withContext(ioDispatcher) {
        db.cashedStreakQueries.deleteStreaksInPeriod(
            habitId = habitId,
            periodStart = periodStartDate,
            periodEnd = periodEndDate,
        )
    }

    private fun CashedStreakEntity.toExternalModel() =
        Streak(
            startDate = startDate,
            endDate = endDate,
        )

    private fun StreakCashedPeriodEntity.toExternalModel() =
        LocalDateRange(
            start = startDate,
            endInclusive = endDate,
        )
}