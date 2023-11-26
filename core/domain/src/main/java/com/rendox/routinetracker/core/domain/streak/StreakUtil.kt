package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus

fun DisplayStreak.contains(date: LocalDate): Boolean = date in startDate..endDate
fun DisplayStreak.getDurationInDays(): Int = startDate.daysUntil(endDate) + 1

fun List<DisplayStreak>.getCurrentStreakTest(today: LocalDate): DisplayStreak? =
    firstOrNull { it.contains(today.minus(DatePeriod(days = 1))) }

fun List<DisplayStreak>.getLongestStreak(): DisplayStreak? =
    maxByOrNull { it.getDurationInDays() }

fun List<DisplayStreak>.mapDateToInclusionStatusInDateRange(
    dateRange: LocalDateRange
): Map<LocalDate, Boolean> {
    val streakDates = mutableMapOf<LocalDate, Boolean>()

    for (date in dateRange) {
        var includedInStreak = false
        for (streak in this) {
            if (streak.contains(date)) {
                includedInStreak = true
                break
            }
        }
        streakDates[date] = includedInStreak
    }

    return streakDates
}