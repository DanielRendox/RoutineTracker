package com.rendox.routinetracker.core.data.streak

import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

interface StreakRepository {
    suspend fun insertStreak(streak: Streak, routineId: Long)
    suspend fun getStreakByDate(routineId: Long, dateWithinStreak: LocalDate): Streak?
    suspend fun getAllStreaks(
        routineId: Long,
        afterDateInclusive: LocalDate? = null,
        beforeDateInclusive: LocalDate? = null,
    ): List<Streak>
    suspend fun getLastStreak(routineId: Long): Streak?
    suspend fun deleteStreakById(id: Long)
    suspend fun updateStreakById(id: Long, start: LocalDate, end: LocalDate?)
}