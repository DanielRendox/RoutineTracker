package com.rendox.routinetracker.core.model

import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.LocalDateRange
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

    abstract val vacationStartDate: LocalDate?
    abstract val vacationEndDate: LocalDate?

    abstract val supportsScheduleDeviation: Boolean
    abstract val supportsPeriodSeparation: Boolean

    sealed class PeriodicSchedule: Schedule() {
        abstract val periodSeparationEnabled: Boolean
        abstract val correspondingPeriod: DatePeriod

        override val supportsScheduleDeviation = true
    }

    sealed class NonPeriodicSchedule: Schedule() {
        override val supportsPeriodSeparation = false
    }

    sealed interface ByNumOfDueDays {
        fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int
    }

    data class EveryDaySchedule(
        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
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

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : WeeklySchedule() {
        override val supportsPeriodSeparation = true

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

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : WeeklySchedule(), ByNumOfDueDays {
        override val periodSeparationEnabled: Boolean = false
        override val supportsPeriodSeparation = false

        private val firstPeriodIsShort
            get() = startDayOfWeek != null && startDate.dayOfWeek != startDayOfWeek

        init {
            check(numOfDueDays <= DateTimeUnit.WEEK.days) {
                "The number of due dates shouldn't be higher than the number of days in week."
            }

            if (firstPeriodIsShort) {
                check(numOfDueDaysInFirstPeriod != null) {
                    "According to the schedule, at the moment of the " +
                            "startDate, the first time period has been already started " +
                            "and hence it's shorter than expected. Therefore, the number of due " +
                            "days for this period is ambiguous. So it should be specified " +
                            "explicitly at the moment of the schedule creation."
                }
            }
        }

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            numOfDueDaysInFirstPeriod?.let {
                val isFirstPeriod = startDate in period
                if (isFirstPeriod) return it
            }
            return numOfDueDays
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
        val weekDaysMonthRelated: List<WeekDayMonthRelated>,
        override val startFromHabitStart: Boolean = true,

        override val periodSeparationEnabled: Boolean = true,
        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : MonthlySchedule() {
        override val supportsPeriodSeparation = true

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

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : MonthlySchedule(), ByNumOfDueDays {
        override val periodSeparationEnabled = false
        override val supportsPeriodSeparation = false

        private val firstPeriodIsShort
            get() = !startFromHabitStart && startDate.dayOfMonth != 1

        init {
            check(numOfDueDays <= 31) {
                "The number of due dates shouldn't be higher than max num of days in month (31)."
            }

            if (firstPeriodIsShort) {
                check(numOfDueDaysInFirstPeriod != null) {
                    "According to the schedule, at the moment of the " +
                            "startDate, the first time period has been already started " +
                            "and hence it's shorter than expected. Therefore, the number of due " +
                            "days for this period is ambiguous. So it should be specified " +
                            "explicitly at the moment of the schedule creation."
                }
            }
        }

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            numOfDueDaysInFirstPeriod?.let {
                val isFirstPeriod = startDate in period
                if (isFirstPeriod) return it
            }
            return numOfDueDays
        }
    }

    data class PeriodicCustomSchedule(
        val numOfDueDays: Int,
        val numOfDaysInPeriod: Int,

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : PeriodicSchedule(), ByNumOfDueDays {
        override val periodSeparationEnabled = false
        override val supportsPeriodSeparation = false

        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = numOfDaysInPeriod)

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            return numOfDueDays
        }
    }

    data class CustomDateSchedule(
        val dueDates: List<LocalDate>,

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
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

        override val periodSeparationEnabled: Boolean = true,
        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : AnnualSchedule() {
        override val supportsPeriodSeparation = true
    }

    data class AnnualScheduleByNumOfDueDays(
        val numOfDueDays: Int,
        val numOfDueDaysInFirstPeriod: Int?,
        override val startFromHabitStart: Boolean,

        override val backlogEnabled: Boolean = true,
        override val completingAheadEnabled: Boolean = true,

        override val startDate: LocalDate,
        override val endDate: LocalDate? = null,

        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
    ) : AnnualSchedule(), ByNumOfDueDays {
        override val periodSeparationEnabled = false
        override val supportsPeriodSeparation = false

        private val firstPeriodIsShort
            get() = !startFromHabitStart && startDate.dayOfYear != 1

        init {
            check(numOfDueDays <= 366) {
                "The number of due dates shouldn't be higher than max num of days in year (366)."
            }

            if (firstPeriodIsShort) {
                check(numOfDueDaysInFirstPeriod != null) {
                    "According to the schedule, at the moment of the " +
                            "startDate, the first time period has been already started " +
                            "and hence it's shorter than expected. Therefore, the number of due " +
                            "days for this period is ambiguous. So it should be specified " +
                            "explicitly at the moment of the schedule creation."
                }
            }
        }

        override fun getNumOfDueDatesInPeriod(period: LocalDateRange): Int {
            numOfDueDaysInFirstPeriod?.let {
                val isFirstPeriod = startDate in period
                if (isFirstPeriod) return it
            }
            return numOfDueDays
        }
    }
}

