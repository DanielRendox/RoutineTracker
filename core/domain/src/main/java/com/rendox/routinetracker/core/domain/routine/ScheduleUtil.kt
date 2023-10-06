package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.matches
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

fun Schedule.isDue(
    validationDate: LocalDate,
    routineStartDate: LocalDate,
): Boolean = when (this) {
    is Schedule.EveryDaySchedule -> everyDayScheduleIsDue()
    is Schedule.WeeklySchedule -> weeklyScheduleIsDue(validationDate)
    is Schedule.MonthlySchedule -> monthlyScheduleIsDue(validationDate)
    is Schedule.PeriodicCustomSchedule -> periodicCustomScheduleIsDue(
        validationDate, routineStartDate,
    )
    is Schedule.CustomDateSchedule -> customDateScheduleIsDue(validationDate)
    is Schedule.AnnualSchedule -> annualScheduleIsDue(validationDate)
}

@Suppress("UnusedReceiverParameter")
private fun Schedule.EveryDaySchedule.everyDayScheduleIsDue(): Boolean = true

private fun Schedule.WeeklySchedule.weeklyScheduleIsDue(
    validationDate: LocalDate,
): Boolean = dueDaysOfWeek.contains(validationDate.dayOfWeek)

private fun Schedule.MonthlySchedule.monthlyScheduleIsDue(
    validationDate: LocalDate,
): Boolean {
    if (includeLastDayOfMonth && validationDate.atEndOfMonth == validationDate) return true

    for (weekDayMonthRelated in weekDaysMonthRelated) {
        if (weekDayMonthRelated.matches(validationDate)) return true
    }

    return dueDatesIndices.contains(validationDate.dayOfMonth)
}

private fun Schedule.PeriodicCustomSchedule.periodicCustomScheduleIsDue(
    validationDate: LocalDate,
    routineStartDate: LocalDate,
): Boolean {
    var daysCounter = routineStartDate.daysUntil(validationDate)
    while (daysCounter % numOfDaysInPeriod != 0) {
        daysCounter--
    }
    val periodStartDate = routineStartDate.plus(DatePeriod(days = daysCounter))
    // start count from 1, not from 0
    val validationDateNumber = periodStartDate.daysUntil(validationDate) + 1
    return dueDatesIndices.contains(validationDateNumber)
}

private fun Schedule.CustomDateSchedule.customDateScheduleIsDue(
    validationDate: LocalDate,
): Boolean = dueDates.contains(validationDate)

private fun Schedule.AnnualSchedule.annualScheduleIsDue(
    validationDate: LocalDate,
): Boolean {
    for (date in dueDates) {
        if (validationDate.month == date.month
            &&
            validationDate.dayOfMonth == date.dayOfMonth
        ) return true
    }
    return false
}