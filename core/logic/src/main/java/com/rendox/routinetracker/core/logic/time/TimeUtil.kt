package com.rendox.routinetracker.core.logic.time

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

val epochDate = LocalDate(1970, 1, 1)

/**
 * Returns this date with another day of month.
 *
 * @throws IllegalArgumentException if the resulting date is invalid or exceeds the platform-specific boundary.
 */
fun LocalDate.withDayOfMonth(dayOfMonth: Int) = LocalDate(this.year, this.month, dayOfMonth)

val LocalDate.atEndOfYear get() = LocalDate(this.year, Month.DECEMBER, 31)

/**
 * The beginning of the next month.
 */
val LocalDate.nextMonth
    get() = withDayOfMonth(1).plus(1, DateTimeUnit.MONTH)
val LocalDate.atEndOfMonth get() = nextMonth - DatePeriod(days = 1)

fun LocalDate.plusDays(daysNumber: Int) = plus(DatePeriod(days = daysNumber))