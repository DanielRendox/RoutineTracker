package com.rendox.routinetracker.core.logic.time

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class WeekDayMonthRelated(
    val dayOfWeek: DayOfWeek,
    val weekDayNumberMonthRelated: WeekDayNumberMonthRelated,
)

enum class WeekDayNumberMonthRelated {
    First,
    Second,
    Third,
    Forth,
    Fifth,
    Last;
}

/**
 * Never returns WeekDayMonthRelated.Last
 */
fun LocalDate.deriveWeekDayRelativeToMonthNumber(): WeekDayNumberMonthRelated {
    val firstWeekDay = this.findDateOfFirstWeekDayInMonth()
    var resultingDate = firstWeekDay
    var counter = 1
    while (resultingDate != this) {
        resultingDate = resultingDate.plus(DatePeriod(days = DateTimeUnit.WEEK.days))
        counter++
    }
    return counter.deriveExplicitWeekDayRelativeToMonth()
}

private fun Int.deriveExplicitWeekDayRelativeToMonth(): WeekDayNumberMonthRelated {
    return when (this) {
        1 -> WeekDayNumberMonthRelated.First
        2 -> WeekDayNumberMonthRelated.Second
        3 -> WeekDayNumberMonthRelated.Third
        4 -> WeekDayNumberMonthRelated.Forth
        5 -> WeekDayNumberMonthRelated.Fifth
        else -> {
            throw IllegalArgumentException("Counter value should be in the range 1..5")
        }
    }
}

fun LocalDate.isLastWeekDayInMonth(): Boolean =
    this.findDateOfLastWeekDayInMonth() == this

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

fun WeekDayMonthRelated.matches(validationDate: LocalDate): Boolean {
    if (validationDate.dayOfWeek != this.dayOfWeek) return false

    if (
        this.weekDayNumberMonthRelated == WeekDayNumberMonthRelated.Last
        &&
        validationDate.isLastWeekDayInMonth()
    ) {
        return true
    }

    val validationDateWeekDayNumberMonthRelated = validationDate
        .deriveWeekDayRelativeToMonthNumber()

    return this.weekDayNumberMonthRelated == validationDateWeekDayNumberMonthRelated
}