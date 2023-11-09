package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun Streak.contains(date: LocalDate): Boolean {
    val streakEnd = end
    return date >= start && (streakEnd == null || streakEnd <= date)
}

fun Streak.getDurationInDays(today: LocalDate): Int {
    val endDate = this.end ?: today
    return this.start.daysUntil(endDate) + 1
}

fun getCurrentStreakDurationInDays(
    lastStreak: Streak?, today: LocalDate, schedule: Schedule
): Int? {
    if (schedule is Schedule.CustomDateSchedule) return null
    if (lastStreak == null) return 0
    val lastStreakEnd = lastStreak.end
    return if (lastStreakEnd == null || lastStreakEnd >= today) {
        lastStreak.copy(end = today).getDurationInDays(today)
    } else 0
}

fun getLongestStreakDurationInDays(
    allStreaks: List<Streak>, today: LocalDate, schedule: Schedule
): Int? {
    if (schedule is Schedule.CustomDateSchedule) return null
    return allStreaks.map { it.getDurationInDays(today) }.maxByOrNull { it } ?: 0
}