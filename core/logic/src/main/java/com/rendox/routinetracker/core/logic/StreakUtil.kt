package com.rendox.routinetracker.core.logic

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus

fun Streak.contains(date: LocalDate): Boolean = date in startDate..endDate
fun Streak.getDurationInDays(): Int = startDate.daysUntil(endDate) + 1
fun Streak.isSubsetOf(period: LocalDateRange): Boolean =
    period.start <= this.startDate && this.endDate <= period.endInclusive

fun List<Streak>.joinAdjacentStreaks(): List<Streak> {
    val resultingStreaks = mutableListOf<Streak>()
    var previousStreak = minByOrNull { it.startDate } ?: return emptyList()

    for (currentStreak in this.sortedBy { it.startDate }.drop(1)) {
        previousStreak = if (previousStreak.endDate.plusDays(1) == currentStreak.startDate) {
            previousStreak.joinWith(currentStreak)
        } else {
            resultingStreaks.add(previousStreak)
            currentStreak
        }
    }
    resultingStreaks.add(previousStreak)

    return resultingStreaks
}

fun Streak.joinWith(other: Streak) = Streak(
    startDate = this.startDate,
    endDate = other.endDate,
)