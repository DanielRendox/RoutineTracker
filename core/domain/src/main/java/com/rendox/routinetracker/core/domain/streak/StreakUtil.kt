package com.rendox.routinetracker.core.domain.streak

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