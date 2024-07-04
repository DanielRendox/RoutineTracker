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
    Last,
}