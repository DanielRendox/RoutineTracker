package com.rendox.routinetracker.core.data.streak

import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class StreakRepositoryImpl(
    private val localDataSource: StreakLocalDataSource
) : StreakRepository {
    override suspend fun insertStreak(streak: Streak, routineId: Long) {
        return localDataSource.insertStreak(streak, routineId)
    }

    override suspend fun getStreakByDate(routineId: Long, dateWithinStreak: LocalDate): Streak? {
        return try {
            localDataSource.getStreakByDate(routineId, dateWithinStreak)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getAllStreaks(
        routineId: Long,
        afterDateInclusive: LocalDate?,
        beforeDateInclusive: LocalDate?,
    ): List<Streak> {
        return localDataSource.getAllStreaks(
            routineId = routineId,
            afterDateInclusive = afterDateInclusive,
            beforeDateInclusive = beforeDateInclusive
        )
    }

    override suspend fun getLastStreak(routineId: Long): Streak? {
        return localDataSource.getLastStreak(routineId)
    }

    override suspend fun deleteStreakById(id: Long) {
        localDataSource.deleteStreakById(id)
    }

    override suspend fun updateStreakById(id: Long, start: LocalDate, end: LocalDate?) {
        localDataSource.updateStreakById(id, start, end)
    }
}