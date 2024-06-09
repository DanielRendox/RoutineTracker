package com.rendox.routinetracker.core.model

import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

sealed class Schedule {

    abstract val startDate: LocalDate
    abstract val endDate: LocalDate?

    abstract val backlogEnabled: Boolean
    abstract val completingAheadEnabled: Boolean

    abstract val supportsScheduleDeviation: Boolean
    val supportsPeriodSeparation: Boolean = false

    sealed class PeriodicSchedule : Schedule() {
        abstract val periodSeparationEnabled: Boolean
        abstract val correspondingPeriod: DatePeriod

        override val supportsScheduleDeviation = true
    }

    sealed class NonPeriodicSchedule : Schedule()

    sealed interface ByNumOfDueDays

    data class EveryDaySchedule(
        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : NonPeriodicSchedule() {
        override val backlogEnabled: Boolean = false
        override val completingAheadEnabled: Boolean = false
        override val supportsScheduleDeviation = false
    }

    sealed class WeeklySchedule : PeriodicSchedule() {
        abstract val startDayOfWeek: DayOfWeek?
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = DateTimeUnit.WEEK.days)
    }

    data class WeeklyScheduleByDueDaysOfWeek(
        val dueDaysOfWeek: List<DayOfWeek>,
        override val startDayOfWeek: DayOfWeek? = null,

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,
        override val periodSeparationEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : WeeklySchedule() {

        init {
            check(dueDaysOfWeek.size <= DateTimeUnit.WEEK.days) {
                "The number of due dates shouldn't be higher than the number of days in week."
            }
        }
    }

    data class WeeklyScheduleByNumOfDueDays(
        val numOfDueDays: Int,
        val numOfDueDaysInFirstPeriod: Int? = null,
        override val startDayOfWeek: DayOfWeek? = null,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val periodSeparationEnabled: Boolean = true,
    ) : WeeklySchedule(), ByNumOfDueDays {
        override val backlogEnabled: Boolean = true
        override val completingAheadEnabled: Boolean = true
        override val supportsScheduleDeviation = false

        init {
            check(numOfDueDays <= DateTimeUnit.WEEK.days) {
                "The number of due dates shouldn't be higher than the number of days in week."
            }
        }
    }

    sealed class MonthlySchedule : PeriodicSchedule() {
        abstract val startFromHabitStart: Boolean

        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(months = 1)
    }

    data class MonthlyScheduleByDueDatesIndices(
        val dueDatesIndices: List<Int>,
        val includeLastDayOfMonth: Boolean = false,
        val weekDaysMonthRelated: List<WeekDayMonthRelated> = emptyList(),
        override val startFromHabitStart: Boolean = true,

        override val periodSeparationEnabled: Boolean = true,
        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : MonthlySchedule() {

        init {
            check(dueDatesIndices.size <= 31) {
                "The number of due dates shouldn't be higher than max num of days in month (31)."
            }
        }
    }

    data class MonthlyScheduleByNumOfDueDays(
        val numOfDueDays: Int,
        val numOfDueDaysInFirstPeriod: Int? = null,
        override val startFromHabitStart: Boolean = true,

        override val periodSeparationEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : MonthlySchedule(), ByNumOfDueDays {
        override val backlogEnabled: Boolean = true
        override val completingAheadEnabled: Boolean = true
        override val supportsScheduleDeviation = false

        init {
            check(numOfDueDays <= 31) {
                "The number of due dates shouldn't be higher than max num of days in month (31)."
            }
        }
    }

    data class AlternateDaysSchedule(
        val numOfDueDays: Int,
        val numOfDaysInPeriod: Int,

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,
        override val periodSeparationEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : PeriodicSchedule(), ByNumOfDueDays {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = numOfDaysInPeriod)
    }

    data class CustomDateSchedule(
        val dueDates: List<LocalDate>,

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : NonPeriodicSchedule() {
        override val supportsScheduleDeviation = true
    }

    sealed class AnnualSchedule : PeriodicSchedule() {
        abstract val startFromHabitStart: Boolean

        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(years = 1)
    }

    data class AnnualScheduleByDueDates(
        val dueDates: List<AnnualDate>,
        override val startFromHabitStart: Boolean,

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,
        override val periodSeparationEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : AnnualSchedule()

    data class AnnualScheduleByNumOfDueDays(
        val numOfDueDays: Int,
        val numOfDueDaysInFirstPeriod: Int?,
        override val startFromHabitStart: Boolean,

        override val periodSeparationEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,
    ) : AnnualSchedule(), ByNumOfDueDays {
        override val backlogEnabled: Boolean = true
        override val completingAheadEnabled: Boolean = true
        override val supportsScheduleDeviation = false

        init {
            check(numOfDueDays <= 366) {
                "The number of due dates shouldn't be higher than max num of days in year (366)."
            }
        }
    }
}

