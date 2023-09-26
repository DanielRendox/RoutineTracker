package com.rendox.routinetracker.core.model

enum class WeekDayRelativeToMonth {
    First,
    Second,
    Third,
    Forth,
    Fifth,
    Last;
}

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