package com.rendox.routinetracker

import com.rendox.routinetracker.routine.model.WeekDayRelativeToMonth
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun Int.deriveExplicitWeekDayRelativeToMonth(): WeekDayRelativeToMonth {
    return when (this) {
        1 -> WeekDayRelativeToMonth.First
        2 -> WeekDayRelativeToMonth.Second
        3 -> WeekDayRelativeToMonth.Third
        4 -> WeekDayRelativeToMonth.Forth
        5 -> WeekDayRelativeToMonth.Fifth
        else -> {
            throw IllegalArgumentException("Counter value should be in the range 1..5")
        }
    }
}

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

/**
 * Returns the last date in month that is the same weekday as the weekday of this date.
 */
fun LocalDate.findDateOfLastWeekDayInMonth(): LocalDate {
    var lastWeekDay = this.atEndOfMonth
    while (lastWeekDay.dayOfWeek != this.dayOfWeek) {
        lastWeekDay = lastWeekDay.minus(DatePeriod(days = 1))
    }
    return lastWeekDay
}

fun LocalDate.findDateOfFirstWeekDayInMonth(): LocalDate {
    var firstWeekDay = this.withDayOfMonth(1)
    while (firstWeekDay.dayOfWeek != this.dayOfWeek) {
        firstWeekDay = firstWeekDay.plus(DatePeriod(days = 1))
    }
    return firstWeekDay
}