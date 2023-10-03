package com.rendox.routinetracker.core.model

import kotlinx.datetime.DayOfWeek

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

fun Int.deriveExplicitWeekDayRelativeToMonth(): WeekDayNumberMonthRelated {
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