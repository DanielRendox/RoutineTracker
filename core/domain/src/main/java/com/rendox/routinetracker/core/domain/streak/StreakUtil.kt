package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus

fun DisplayStreak.contains(date: LocalDate): Boolean = date in startDate..endDate
fun DisplayStreak.getDurationInDays(): Int = startDate.daysUntil(endDate) + 1

fun List<DisplayStreak>.getCurrentStreak(today: LocalDate): DisplayStreak? =
    firstOrNull {
        val yesterdayWasInStreak = it.contains(today.minus(DatePeriod(days = 1)))
        if (yesterdayWasInStreak) true
        else it.contains(today)
    }

fun List<DisplayStreak>.getLongestStreak(): DisplayStreak? =
    maxByOrNull { it.getDurationInDays() }

fun List<DisplayStreak>.checkIfContainDate(date: LocalDate): Boolean {
    var includedInStreak = false
    for (streak in this) {
        if (streak.contains(date)) {
            includedInStreak = true
            break
        }
    }
    return includedInStreak
}