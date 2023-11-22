package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.matches
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun Schedule.isDue(
    validationDate: LocalDate,
    actualDate: LocalDate?,
    numOfTimesCompletedInCurrentPeriod: Double,
): Boolean {
    if (validationDate < routineStartDate) return false
    routineEndDate?.let { if (validationDate > it) return false }

    return when (this) {
        is Schedule.EveryDaySchedule -> everyDayScheduleIsDue()

        is Schedule.WeeklyScheduleByDueDaysOfWeek -> weeklyScheduleByDueDaysOfWeekIsDue(
            validationDate
        )

        is Schedule.WeeklyScheduleByNumOfDueDays -> {
            val period = getPeriodRange(validationDate)
            scheduleByNumOfDueDaysIsDue(
                validationDate = validationDate,
                validationDatePeriod = period,
                numOfDueDays = getNumOfDueDatesInPeriod(period),
                numOfCompletedDays = numOfTimesCompletedInCurrentPeriod,
                dateScheduleDeviationIsActualFor = actualDate,
            )
        }

        is Schedule.MonthlyScheduleByDueDatesIndices -> monthlyScheduleIsDue(validationDate)

        is Schedule.MonthlyScheduleByNumOfDueDays -> {
            val period = getPeriodRange(validationDate)
            scheduleByNumOfDueDaysIsDue(
                validationDate = validationDate,
                validationDatePeriod = period,
                numOfDueDays = getNumOfDueDatesInPeriod(period),
                numOfCompletedDays = numOfTimesCompletedInCurrentPeriod,
                dateScheduleDeviationIsActualFor = actualDate,
            )
        }

        is Schedule.PeriodicCustomSchedule -> {
            val period = getPeriodRange(validationDate)
            scheduleByNumOfDueDaysIsDue(
                validationDate = validationDate,
                validationDatePeriod = period,
                numOfDueDays = getNumOfDueDatesInPeriod(period),
                numOfCompletedDays = numOfTimesCompletedInCurrentPeriod,
                dateScheduleDeviationIsActualFor = actualDate,
            )
        }

        is Schedule.CustomDateSchedule -> customDateScheduleIsDue(validationDate)

        is Schedule.AnnualScheduleByDueDates -> annualScheduleIsDue(validationDate)

        is Schedule.AnnualScheduleByNumOfDueDays -> {
            val period = getPeriodRange(validationDate)
            scheduleByNumOfDueDaysIsDue(
                validationDate = validationDate,
                validationDatePeriod = period,
                numOfDueDays = getNumOfDueDatesInPeriod(period),
                numOfCompletedDays = numOfTimesCompletedInCurrentPeriod,
                dateScheduleDeviationIsActualFor = actualDate,
            )
        }
    }
}

@Suppress("UnusedReceiverParameter")
private fun Schedule.EveryDaySchedule.everyDayScheduleIsDue(): Boolean = true

private fun Schedule.WeeklyScheduleByDueDaysOfWeek.weeklyScheduleByDueDaysOfWeekIsDue(
    validationDate: LocalDate,
): Boolean = dueDaysOfWeek.contains(validationDate.dayOfWeek)

private fun Schedule.MonthlyScheduleByDueDatesIndices.monthlyScheduleIsDue(
    validationDate: LocalDate,
): Boolean {
    if (includeLastDayOfMonth && validationDate.atEndOfMonth == validationDate) return true

    for (weekDayMonthRelated in weekDaysMonthRelated) {
        if (weekDayMonthRelated.matches(validationDate)) return true
    }

    return dueDatesIndices.contains(validationDate.dayOfMonth)
}

private fun Schedule.CustomDateSchedule.customDateScheduleIsDue(
    validationDate: LocalDate,
): Boolean = dueDates.contains(validationDate)

private fun Schedule.AnnualScheduleByDueDates.annualScheduleIsDue(
    validationDate: LocalDate,
): Boolean {
    for (date in dueDates) {
        if (validationDate.month == date.month && validationDate.dayOfMonth == date.dayOfMonth) return true
    }
    return false
}

@Suppress("UnusedReceiverParameter")
private fun Schedule.ByNumOfDueDays.scheduleByNumOfDueDaysIsDue(
    validationDate: LocalDate,
    validationDatePeriod: LocalDateRange,
    numOfDueDays: Int,
    numOfCompletedDays: Double,
    dateScheduleDeviationIsActualFor: LocalDate?,
): Boolean {
    val validationDateNumberInPeriod = validationDatePeriod.start.daysUntil(validationDate) + 1
    if (validationDateNumberInPeriod <= numOfDueDays) return true

    dateScheduleDeviationIsActualFor?.let {
        val daysBetweenLastDateInHistoryAndValidationDate = it.daysUntil(validationDate) - 1
        val daysExpectedToBeCompletedTillThatTime =
            numOfCompletedDays + daysBetweenLastDateInHistoryAndValidationDate.toDouble()
        if (daysExpectedToBeCompletedTillThatTime < (numOfDueDays.toDouble())) return true
    }

    return false
}