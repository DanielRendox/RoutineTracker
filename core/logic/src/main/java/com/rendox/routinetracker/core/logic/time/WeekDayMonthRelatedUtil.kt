package com.rendox.routinetracker.core.logic.time

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 *
 */
fun LocalDate.deriveWeekDayRelativeToMonthNumber(): Int {
    val firstWeekDay = this.findDateOfFirstWeekDayInMonth()
    var resultingDate = firstWeekDay
    var counter = 1
    while (resultingDate != this) {
        resultingDate = resultingDate.plus(DatePeriod(days = DateTimeUnit.WEEK.days))
        counter++
    }
    return counter
}

fun LocalDate.isLastWeekDayInMonth(): Boolean =
    this.findDateOfFirstWeekDayInMonth() == this

/**
 * Returns the last date in month that is the same weekday as the weekday of this date.
 */
private fun LocalDate.findDateOfLastWeekDayInMonth(): LocalDate {
    var lastWeekDay = this.atEndOfMonth
    while (lastWeekDay.dayOfWeek != this.dayOfWeek) {
        lastWeekDay = lastWeekDay.minus(DatePeriod(days = 1))
    }
    return lastWeekDay
}

private fun LocalDate.findDateOfFirstWeekDayInMonth(): LocalDate {
    var firstWeekDay = this.withDayOfMonth(1)
    while (firstWeekDay.dayOfWeek != this.dayOfWeek) {
        firstWeekDay = firstWeekDay.plus(DatePeriod(days = 1))
    }
    return firstWeekDay
}