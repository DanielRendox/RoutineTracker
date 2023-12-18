package com.rendox.routinetracker.core.domain.completion_history.schedule

import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.matches
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun Schedule.isDue(
    validationDate: LocalDate,
    lastVacationEndDate: LocalDate? = null,
): Boolean = when (this) {
    is Schedule.EveryDaySchedule -> true

    is Schedule.ByNumOfDueDays -> scheduleByNumOfDueDaysIsDue(
        validationDate = validationDate,
        lastVacationEndDate = lastVacationEndDate,
    )

    is Schedule.WeeklyScheduleByDueDaysOfWeek -> weeklyScheduleByDueDaysOfWeekIsDue(
        validationDate = validationDate
    )

    is Schedule.MonthlyScheduleByDueDatesIndices -> monthlyScheduleIsDue(
        validationDate = validationDate
    )

    is Schedule.CustomDateSchedule -> customDateScheduleIsDue(
        validationDate = validationDate
    )

    is Schedule.AnnualScheduleByDueDates -> annualScheduleIsDue(
        validationDate = validationDate
    )
}

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

private fun Schedule.ByNumOfDueDays.scheduleByNumOfDueDaysIsDue(
    validationDate: LocalDate,
    lastVacationEndDate: LocalDate?,
): Boolean {
    val validationDatePeriod = (this as Schedule.PeriodicSchedule).getPeriodRange(
        currentDate = validationDate,
        lastVacationEndDate = lastVacationEndDate,
    ) ?: return false
    val validationDateNumber = validationDatePeriod.start.daysUntil(validationDate) + 1
    return validationDateNumber <= getNumOfDueDatesInPeriod(validationDatePeriod)
}