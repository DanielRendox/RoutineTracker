package com.rendox.routinetracker.core.data.streaks

import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

interface StreakRepository {
    fun insertStreak(routineId: Long, streak: Streak)
    fun getStreakByDate(routineId: Long, dateWithinStreak: LocalDate): Streak?
    fun deleteStreakById(id: Long)
    fun updateStreakById(id: Long, newValue: Streak)
    fun getAllStreaks(routineId: Long): List<Streak>
}