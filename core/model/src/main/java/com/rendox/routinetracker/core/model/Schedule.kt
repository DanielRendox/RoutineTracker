package com.rendox.routinetracker.core.model

import com.rendox.routinetracker.core.logic.time.AnnualDate
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
    abstract val periodSeparationEnabled: Boolean

    abstract val vacationStartDate: LocalDate?
    abstract val vacationEndDate: LocalDate?

    abstract val numOfDueDaysInStandardPeriod: Int
    abstract val correspondingPeriod: DatePeriod?

    data class EveryDaySchedule(
        override val routineStartDate: LocalDate,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        override val routineEndDate: LocalDate? = null,
    ) : Schedule() {

        override val numOfDueDaysInStandardPeriod: Int
            get() = 1

        override val periodSeparationEnabled: Boolean = false

        override val correspondingPeriod: DatePeriod?
            get() = null
    }

    data class WeeklySchedule(
        override val routineStartDate: LocalDate,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val periodSeparationEnabled: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        override val routineEndDate: LocalDate? = null,
        val dueDaysOfWeek: List<DayOfWeek>,
        val startDayOfWeek: DayOfWeek? = null,
    ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = DateTimeUnit.WEEK.days)
        override val numOfDueDaysInStandardPeriod: Int
            get() = dueDaysOfWeek.size
    }

    data class MonthlySchedule(
        override val routineStartDate: LocalDate,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val periodSeparationEnabled: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        override val routineEndDate: LocalDate? = null,
        val dueDatesIndices: List<Int>,
        val includeLastDayOfMonth: Boolean = false,
        val weekDaysMonthRelated: List<WeekDayMonthRelated>,
        val startFromRoutineStart: Boolean,
        ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(months = 1)
        override val numOfDueDaysInStandardPeriod: Int
            get() {
                var dueDaysCounter = dueDatesIndices.size
                if (includeLastDayOfMonth) dueDaysCounter++
                dueDaysCounter + weekDaysMonthRelated.size
                return dueDaysCounter
            }
    }

    data class PeriodicCustomSchedule(
        override val routineStartDate: LocalDate,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val periodSeparationEnabled: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        override val routineEndDate: LocalDate? = null,
        val dueDatesIndices: List<Int>,
        val numOfDaysInPeriod: Int,
    ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(days = numOfDaysInPeriod)
        override val numOfDueDaysInStandardPeriod: Int
            get() = dueDatesIndices.size
    }

    data class CustomDateSchedule(
        override val routineStartDate: LocalDate,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        override val routineEndDate: LocalDate? = null,
        val dueDates: List<LocalDate>,
    ) : Schedule() {
        override val numOfDueDaysInStandardPeriod: Int
            get() = dueDates.size

        override val periodSeparationEnabled: Boolean = false

        override val correspondingPeriod: DatePeriod?
            get() = null
    }

    data class AnnualSchedule(
        override val routineStartDate: LocalDate,
        override val backlogEnabled: Boolean = false,
        override val cancelDuenessIfDoneAhead: Boolean = false,
        override val vacationStartDate: LocalDate? = null,
        override val vacationEndDate: LocalDate? = null,
        override val routineEndDate: LocalDate? = null,
        override val periodSeparationEnabled: Boolean = false,
        val dueDates: List<AnnualDate>,
        val startFromRoutineStart: Boolean,
        ) : Schedule() {
        override val correspondingPeriod: DatePeriod
            get() = DatePeriod(years = 1)
        override val numOfDueDaysInStandardPeriod: Int
            get() = dueDates.size
    }
}

