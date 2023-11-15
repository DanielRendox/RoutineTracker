package com.rendox.routinetracker.core.domain.routine.schedule

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.matches
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun Schedule.isDue(
    validationDate: LocalDate,
    dateScheduleDeviationIsActualFor: LocalDate?,
): Boolean {
    if (validationDate < routineStartDate) {
        throw IllegalArgumentException(
            "This function shouldn't be called for dates that are prior to routine start date.\n" +
                    "validationDate = $validationDate; routineStartDate = $routineStartDate"
        )
    }

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
                numOfCompletedDays = numOfCompletedDaysInCurrentPeriod,
                dateScheduleDeviationIsActualFor = dateScheduleDeviationIsActualFor,
            )
        }

        is Schedule.MonthlyScheduleByDueDatesIndices -> monthlyScheduleIsDue(validationDate)

        is Schedule.MonthlyScheduleByNumOfDueDays -> {
            val period = getPeriodRange(validationDate)
            scheduleByNumOfDueDaysIsDue(
                validationDate = validationDate,
                validationDatePeriod = period,
                numOfDueDays = getNumOfDueDatesInPeriod(period),
                numOfCompletedDays = numOfCompletedDaysInCurrentPeriod,
                dateScheduleDeviationIsActualFor = dateScheduleDeviationIsActualFor,
            )
        }

        is Schedule.PeriodicCustomSchedule -> {
            val period = getPeriodRange(validationDate)
            scheduleByNumOfDueDaysIsDue(
                validationDate = validationDate,
                validationDatePeriod = period,
                numOfDueDays = getNumOfDueDatesInPeriod(period),
                numOfCompletedDays = numOfCompletedDaysInCurrentPeriod,
                dateScheduleDeviationIsActualFor = dateScheduleDeviationIsActualFor,
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
                numOfCompletedDays = numOfCompletedDaysInCurrentPeriod,
                dateScheduleDeviationIsActualFor = dateScheduleDeviationIsActualFor,
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

private fun Schedule.PeriodicSchedule.scheduleByNumOfDueDaysIsDue(
    validationDate: LocalDate,
    validationDatePeriod: LocalDateRange,
    numOfDueDays: Int,
    numOfCompletedDays: Int,
    dateScheduleDeviationIsActualFor: LocalDate?,
): Boolean {
    val validationDateNumberInPeriod = validationDatePeriod.start.daysUntil(validationDate) + 1
    if (validationDateNumberInPeriod <= numOfDueDays) return true

    dateScheduleDeviationIsActualFor?.let {
        val daysBetweenLastDateInHistoryAndValidationDate = it.daysUntil(validationDate) - 1
        val daysExpectedToBeCompletedTillThatTime =
            numOfCompletedDays + daysBetweenLastDateInHistoryAndValidationDate
        if (daysExpectedToBeCompletedTillThatTime < numOfDueDays) return true
    }

    return false
}