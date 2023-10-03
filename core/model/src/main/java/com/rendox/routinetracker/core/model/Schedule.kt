package com.rendox.routinetracker.core.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

sealed interface Schedule {

    object EveryDaySchedule : Schedule

    data class WeeklySchedule(
        val dueDaysOfWeek: List<DayOfWeek>,
        val startDayOfWeek: DayOfWeek?,
    ) : Schedule

    data class MonthlySchedule(
        val dueDatesIndices: List<Int>,
        val includeLastDayOfMonth: Boolean,
        val weekDaysMonthRelated: List<WeekDayMonthRelated>?,
    ) : Schedule

    data class PeriodicCustomSchedule(
        val dueDatesIndices: List<Int>,
        val numOfDays: Int,
    ) : Schedule

    data class CustomDateSchedule(
        val dueDates: List<LocalDate>,
    ) : Schedule
}

