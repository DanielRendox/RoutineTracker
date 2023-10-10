package com.rendox.routinetracker.core.model

import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

sealed class Schedule {

    abstract val startDate: LocalDate
    abstract val scheduleDeviation: Int
    abstract val backlogEnabled: Boolean
    abstract val cancelDuenessIfDoneAhead: Boolean
    abstract val vacationStartDate: LocalDate?
    abstract val vacationEndDate: LocalDate?

    abstract val correspondingPeriod: DatePeriod
    abstract val numOfDueDays: Int

    data class EveryDaySchedule(
        override val startDate: LocalDate,
        override val scheduleDeviation: Int = 0,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = 1)

        override val numOfDueDays: Int
            get() = 1
    }

    data class WeeklySchedule(
        override val startDate: LocalDate,
        override val scheduleDeviation: Int = 0,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        val dueDaysOfWeek: List<DayOfWeek>,
        val startDayOfWeek: DayOfWeek? = null,
    ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = DateTimeUnit.WEEK.days)
        override val numOfDueDays: Int
            get() = dueDaysOfWeek.size
    }

    data class MonthlySchedule(
        override val startDate: LocalDate,
        override val scheduleDeviation: Int = 0,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        val dueDatesIndices: List<Int>,
        val includeLastDayOfMonth: Boolean = false,
        val weekDaysMonthRelated: List<WeekDayMonthRelated>,
        val startFromRoutineStart: Boolean,
        ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(months = 1)
        override val numOfDueDays: Int
            get() {
                var dueDaysCounter = dueDatesIndices.size
                if (includeLastDayOfMonth) dueDaysCounter++
                dueDaysCounter + weekDaysMonthRelated.size
                return dueDaysCounter
            }
    }

    data class PeriodicCustomSchedule(
        override val startDate: LocalDate,
        override val scheduleDeviation: Int = 0,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        val dueDatesIndices: List<Int>,
        val numOfDaysInPeriod: Int,
    ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = numOfDaysInPeriod)
        override val numOfDueDays: Int
            get() = dueDatesIndices.size
    }

    data class CustomDateSchedule(
        override val startDate: LocalDate,
        override val scheduleDeviation: Int = 0,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        val dueDates: List<LocalDate>,
    ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = dueDates.size)
        override val numOfDueDays: Int
            get() = dueDates.size
    }

    data class AnnualSchedule(
        override val startDate: LocalDate,
        override val scheduleDeviation: Int = 0,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        val dueDates: List<AnnualDate>,
        val startDayOfYear: AnnualDate?,
        ) : Schedule() {
        init {
            check(startDayOfYear != AnnualDate(Month.FEBRUARY, 29)) {
                "Start day of year should be consistent throughout years."
            }
        }
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(years = 1)
        override val numOfDueDays: Int
            get() = dueDates.size
    }
}

