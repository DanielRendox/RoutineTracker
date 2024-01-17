package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus

fun Streak.contains(date: LocalDate): Boolean = date in startDate..endDate
fun Streak.getDurationInDays(): Int = startDate.daysUntil(endDate) + 1

fun List<Streak>.getCurrentStreak(today: LocalDate): Streak? =
    firstOrNull {
        val yesterdayWasInStreak = it.contains(today.minus(DatePeriod(days = 1)))
        if (yesterdayWasInStreak) true
        else it.contains(today)
    }

fun List<Streak>.getLongestStreak(): Streak? =
    maxByOrNull { it.getDurationInDays() }

fun List<Streak>.joinAdjacentStreaks(): List<Streak> {
    val resultingStreaks = mutableListOf<Streak>()
    var previousStreak = firstOrNull() ?: return emptyList()

    for (currentStreak in this.drop(1)) {
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