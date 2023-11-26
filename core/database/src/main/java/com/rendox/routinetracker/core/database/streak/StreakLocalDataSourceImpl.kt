package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class StreakLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val dispatcher: CoroutineDispatcher,
) : StreakLocalDataSource {

    override suspend fun getAllStreaks(
        routineId: Long,
        afterDateInclusive: LocalDate?,
        beforeDateInclusive: LocalDate?,
    ): List<Streak> {
        return withContext(dispatcher) {
            db.streakEntityQueries.getAllStreaks(
                routineId = routineId,
                afterDateInclusive = afterDateInclusive,
                beforeDateInclusive = beforeDateInclusive,
            ).executeAsList().map { it.toExternalModel() }
        }
    }

    override suspend fun getStreakByDate(routineId: Long, dateWithinStreak: LocalDate): Streak? {
        return withContext(dispatcher) {
            db.streakEntityQueries.getStreakByDate(
                routineId, dateWithinStreak
            ).executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getLastStreak(routineId: Long): Streak? {
        return withContext(dispatcher) {
            db.streakEntityQueries.getLastStreak(routineId)
                .executeAsOneOrNull()
                ?.toExternalModel()
        }
    }

    override suspend fun insertStreak(streak: Streak, routineId: Long) {
        withContext(dispatcher) {
            db.streakEntityQueries.insertStreak(
                id = streak.id,
                routineId = routineId,
                startDate = streak.startDate,
                endDate = streak.endDate,
            )
        }
    }

    override suspend fun deleteStreakById(id: Long) {
        withContext(dispatcher) {
            db.streakEntityQueries.deleteStreakById(id)
        }
    }

    override suspend fun updateStreakById(id: Long, start: LocalDate, end: LocalDate?) {
        withContext(dispatcher) {
            db.streakEntityQueries.updateStreakById(
                id = id,
                startDate = start,
                endDate = end,
            )
        }
    }

    private fun StreakEntity.toExternalModel() = Streak(
        id = id,
        startDate = startDate,
        endDate = endDate,
    )
}