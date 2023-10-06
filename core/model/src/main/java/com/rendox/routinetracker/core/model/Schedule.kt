package com.rendox.routinetracker.core.model

import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

sealed interface Schedule {

    val correspondingPeriod: DatePeriod

    object EveryDaySchedule : Schedule {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = 1)
    }

    data class WeeklySchedule(
        val dueDaysOfWeek: List<DayOfWeek>,
        val startDayOfWeek: DayOfWeek? = null,
    ) : Schedule {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = DateTimeUnit.WEEK.days)
    }

    data class MonthlySchedule(
        val dueDatesIndices: List<Int>,
        val includeLastDayOfMonth: Boolean = false,
        val weekDaysMonthRelated: List<WeekDayMonthRelated>,
        val startFromRoutineStart: Boolean,
    ) : Schedule {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(months = 1)
    }

    data class PeriodicCustomSchedule(
        val dueDatesIndices: List<Int>,
        val numOfDaysInPeriod: Int,
    ) : Schedule {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = numOfDaysInPeriod)
    }

    data class CustomDateSchedule(
        val dueDates: List<LocalDate>,
    ) : Schedule {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = dueDates.size)
    }

    data class AnnualSchedule(
        val dueDates: List<AnnualDate>,
        val startDayOfYear: AnnualDate?,
    ) : Schedule {
        init {
            check(startDayOfYear != AnnualDate(Month.FEBRUARY, 29)) {
                "Start day of year should be consistent throughout years."
            }
        }
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(years = 1)
    }
}

