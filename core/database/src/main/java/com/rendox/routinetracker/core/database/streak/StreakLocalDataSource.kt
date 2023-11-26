package com.rendox.routinetracker.core.database.streak

import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

interface StreakLocalDataSource {

    suspend fun getAllStreaks(
        routineId: Long,
        afterDateInclusive: LocalDate? = null,
        beforeDateInclusive: LocalDate? = null,
    ): List<Streak>

    suspend fun getStreakByDate(routineId: Long, dateWithinStreak: LocalDate): Streak?
    suspend fun getLastStreak(routineId: Long): Streak?
    suspend fun insertStreak(streak: Streak, routineId: Long)
    suspend fun deleteStreakById(id: Long)
    suspend fun updateStreakById(id: Long, start: LocalDate, end: LocalDate?)
}