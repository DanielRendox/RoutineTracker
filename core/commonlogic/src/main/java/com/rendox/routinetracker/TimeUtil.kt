package com.rendox.routinetracker

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Returns this date with another day of month.
 *
 * @throws IllegalArgumentException if the resulting date is invalid or exceeds the platform-specific boundary.
 */
fun LocalDate.withDayOfMonth(dayOfMonth: Int) = LocalDate(this.year, this.month, dayOfMonth)

/**
 * The beginning of the next month.
 */
val LocalDate.nextMonth
    get() = withDayOfMonth(1).plus(1, DateTimeUnit.MONTH)
val LocalDate.atEndOfMonth get() = nextMonth - DatePeriod(days = 1)