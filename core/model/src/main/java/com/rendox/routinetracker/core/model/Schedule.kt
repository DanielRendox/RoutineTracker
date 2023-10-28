package com.rendox.routinetracker.core.model

import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

sealed class Schedule {

    abstract val routineStartDate: LocalDate
    abstract val routineEndDate: LocalDate?

    abstract val backlogEnabled: Boolean
    abstract val cancelDuenessIfDoneAhead: Boolean

    abstract val vacationStartDate: LocalDate?
    abstract val vacationEndDate: LocalDate?

    abstract val lastDateInHistory: LocalDate?

    sealed class PeriodicSchedule: Schedule() {
        abstract val periodSeparationEnabled: Boolean
        abstract val correspondingPeriod: DatePeriod
    }

    sealed class NonPeriodicSchedule: Schedule()

    sealed interface ByNumOfDueDays {
        fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int
    }

    data class EveryDaySchedule(
        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : NonPeriodicSchedule() {
        override val backlogEnabled: Boolean = false
        override val cancelDuenessIfDoneAhead: Boolean = false
    }

    sealed class WeeklySchedule : PeriodicSchedule() {
        abstract val startDayOfWeek: DayOfWeek?

        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = DateTimeUnit.WEEK.days)
    }

    data class WeeklyScheduleByDueDaysOfWeek(
        val dueDaysOfWeek: List<DayOfWeek>,
        override val startDayOfWeek: DayOfWeek? = null,

        override val periodSeparationEnabled: Boolean = false,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : WeeklySchedule() {

        init {
            check(dueDaysOfWeek.size <= DateTimeUnit.WEEK.days) {
                "The number of due dates shouldn't be higher than the number of days in week."
            }
        }
    }

    data class WeeklyScheduleByNumOfDueDays(
        val numOfDueDays: Int,
        val numOfDueDaysInFirstPeriod: Int?,
        val numOfCompletedDaysInCurrentPeriod: Int = 0,
        override val startDayOfWeek: DayOfWeek? = null,

        override val periodSeparationEnabled: Boolean = false,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : WeeklySchedule(), ByNumOfDueDays {

        private val firstPeriodIsShort
            get() = startDayOfWeek != null && routineStartDate.dayOfWeek != startDayOfWeek

        init {
            check(numOfDueDays <= DateTimeUnit.WEEK.days) {
                "The number of due dates shouldn't be higher than the number of days in week."
            }

            if (firstPeriodIsShort) {
                check(numOfDueDaysInFirstPeriod != null) {
                    "According to the routine's schedule, at the moment of the " +
                            "routineStartDate, the first time period has been already started " +
                            "and hence it's shorter than expected. Therefore, the number of due " +
                            "days for this period is ambiguous. So it should be specified " +
                            "explicitly at the moment of the schedule (routine) creation."
                }
            }
        }

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            numOfDueDaysInFirstPeriod?.let {
                val isFirstPeriod = routineStartDate in period
                if (isFirstPeriod) return it
            }
            return numOfDueDays
        }
    }

    sealed class MonthlySchedule : PeriodicSchedule() {
        abstract val startFromRoutineStart: Boolean

        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(months = 1)
    }

    data class MonthlyScheduleByDueDatesIndices(
        val dueDatesIndices: List<Int>,
        val includeLastDayOfMonth: Boolean = false,
        val weekDaysMonthRelated: List<WeekDayMonthRelated>,
        override val startFromRoutineStart: Boolean,

        override val periodSeparationEnabled: Boolean = false,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : MonthlySchedule() {

        init {
            check(dueDatesIndices.size <= 31) {
                "The number of due dates shouldn't be higher than max num of days in month (31)."
            }
        }
    }

    data class MonthlyScheduleByNumOfDueDays(
        val numOfDueDays: Int,
        val numOfDueDaysInFirstPeriod: Int?,
        val numOfCompletedDaysInCurrentPeriod: Int = 0,
        override val startFromRoutineStart: Boolean,

        override val periodSeparationEnabled: Boolean = false,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : MonthlySchedule(), ByNumOfDueDays {

        private val firstPeriodIsShort
            get() = !startFromRoutineStart && routineStartDate.dayOfMonth != 1

        init {
            check(numOfDueDays <= 31) {
                "The number of due dates shouldn't be higher than max num of days in month (31)."
            }

            if (firstPeriodIsShort) {
                check(numOfDueDaysInFirstPeriod != null) {
                    "According to the routine's schedule, at the moment of the " +
                            "routineStartDate, the first time period has been already started " +
                            "and hence it's shorter than expected. Therefore, the number of due " +
                            "days for this period is ambiguous. So it should be specified " +
                            "explicitly at the moment of the schedule (routine) creation."
                }
            }
        }

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            numOfDueDaysInFirstPeriod?.let {
                val isFirstPeriod = routineStartDate in period
                if (isFirstPeriod) return it
            }
            return numOfDueDays
        }
    }

    data class PeriodicCustomSchedule(
        val numOfDueDays: Int,
        val numOfDaysInPeriod: Int,
        val numOfCompletedDaysInCurrentPeriod: Int = 0,

        override val periodSeparationEnabled: Boolean = false,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : PeriodicSchedule(), ByNumOfDueDays {

        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = numOfDaysInPeriod)

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            return numOfDueDays
        }
    }

    data class CustomDateSchedule(
        val dueDates: List<LocalDate>,

        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : NonPeriodicSchedule()

    sealed class AnnualSchedule : PeriodicSchedule() {
        abstract val startFromRoutineStart: Boolean

        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(years = 1)
    }

    data class AnnualScheduleByDueDates(
        val dueDates: List<AnnualDate>,
        override val startFromRoutineStart: Boolean,

        override val periodSeparationEnabled: Boolean = false,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : AnnualSchedule()

    data class AnnualScheduleByNumOfDueDays(
        val numOfDueDays: Int,
        val numOfDueDaysInFirstPeriod: Int?,
        val numOfCompletedDaysInCurrentPeriod: Int = 0,
        override val startFromRoutineStart: Boolean,

        override val periodSeparationEnabled: Boolean = false,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,

        override val routineStartDate: LocalDate,
        override val routineEndDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,

        override val lastDateInHistory: LocalDate? = null
    ) : AnnualSchedule(), ByNumOfDueDays {

        private val firstPeriodIsShort
            get() = !startFromRoutineStart && routineStartDate.dayOfYear != 1

        init {
            check(numOfDueDays <= 366) {
                "The number of due dates shouldn't be higher than max num of days in year (366)."
            }

            if (firstPeriodIsShort) {
                check(numOfDueDaysInFirstPeriod != null) {
                    "According to the routine's schedule, at the moment of the " +
                            "routineStartDate, the first time period has been already started " +
                            "and hence it's shorter than expected. Therefore, the number of due " +
                            "days for this period is ambiguous. So it should be specified " +
                            "explicitly at the moment of the schedule (routine) creation."
                }
            }
        }

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            numOfDueDaysInFirstPeriod?.let {
                val isFirstPeriod = routineStartDate in period
                if (isFirstPeriod) return it
            }
            return numOfDueDays
        }
    }
}

