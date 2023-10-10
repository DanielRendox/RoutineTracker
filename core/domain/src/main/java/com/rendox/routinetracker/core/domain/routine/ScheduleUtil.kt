package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.matches
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.until
import kotlinx.datetime.yearsUntil

fun Schedule.isDue(
    validationDate: LocalDate,
): Boolean = when (this) {
    is Schedule.EveryDaySchedule -> everyDayScheduleIsDue()
    is Schedule.WeeklySchedule -> weeklyScheduleIsDue(validationDate)
    is Schedule.MonthlySchedule -> monthlyScheduleIsDue(validationDate)
    is Schedule.PeriodicCustomSchedule -> periodicCustomScheduleIsDue(validationDate, startDate)
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


fun Schedule.getPeriodRange(currentDate: LocalDate): LocalDateRange {
    check(currentDate >= startDate) {
        "The routine did not exist at the specified date"
    }

    return when (this) {
        is Schedule.EveryDaySchedule -> everyDayScheduleGetPeriodRange(currentDate)
        is Schedule.WeeklySchedule -> weeklyScheduleGetPeriodRange(currentDate)
        is Schedule.MonthlySchedule -> monthlyScheduleGetPeriodRange(currentDate)
        is Schedule.AnnualSchedule -> annualScheduleGetPeriodRange(currentDate)
        else -> defaultGetPeriodRange(currentDate)
    }
}

@Suppress("UnusedReceiverParameter")
private fun Schedule.EveryDaySchedule.everyDayScheduleGetPeriodRange(
    currentDate: LocalDate,
): LocalDateRange = currentDate..currentDate

private fun Schedule.WeeklySchedule.weeklyScheduleGetPeriodRange(
    currentDate: LocalDate,
): LocalDateRange {
    var startPeriodDate: LocalDate
    var endPeriodDate: LocalDate

    val startFromRoutineStart = startDayOfWeek == null
    if (startFromRoutineStart) {
        val unit = DateTimeUnit.WEEK
        val numberOfPeriodsAlreadyPassed = startDate.until(currentDate, unit)
        startPeriodDate = startDate.plus(numberOfPeriodsAlreadyPassed, unit)
        endPeriodDate = startPeriodDate.atEndOfPeriod(correspondingPeriod)
    } else {
        startPeriodDate = currentDate
        while (startPeriodDate.dayOfWeek != startDayOfWeek && startPeriodDate != startDate) {
            startPeriodDate = startPeriodDate.minus(correspondingPeriod)
        }
        val endDateDayOfWeek = startPeriodDate.minus(correspondingPeriod).dayOfWeek
        endPeriodDate = currentDate
        while (endPeriodDate.dayOfWeek != endDateDayOfWeek) {
            endPeriodDate = endPeriodDate.plus(correspondingPeriod)
        }
    }

    return startPeriodDate..endPeriodDate
}

private fun Schedule.MonthlySchedule.monthlyScheduleGetPeriodRange(
    currentDate: LocalDate,
): LocalDateRange {
    val startPeriodDate: LocalDate
    val endPeriodDate: LocalDate

    if (startFromRoutineStart) {
        val numberOfPeriodsAlreadyPassed = startDate.monthsUntil(currentDate)
        startPeriodDate = startDate.plus(DatePeriod(months = numberOfPeriodsAlreadyPassed))
        endPeriodDate = startPeriodDate.atEndOfPeriod(correspondingPeriod)
    } else {
        val stillFirstMonth = startDate.plus(correspondingPeriod) > currentDate
        if (stillFirstMonth) {
            startPeriodDate = startDate
            endPeriodDate = currentDate.atEndOfMonth
        } else {
            startPeriodDate = LocalDate(currentDate.year, currentDate.month, 1)
            endPeriodDate = startPeriodDate.atEndOfPeriod(correspondingPeriod)
        }
    }

    return startPeriodDate..endPeriodDate
}

private fun Schedule.AnnualSchedule.annualScheduleGetPeriodRange(
    currentDate: LocalDate,
): LocalDateRange {
    val startPeriodDate: LocalDate
    val endPeriodDate: LocalDate

    val startFromRoutineStart = startDayOfYear == null
    if (startFromRoutineStart) {
        val numberOfPeriodsAlreadyPassed = startDate.yearsUntil(currentDate)
        startPeriodDate = startDate.plus(DatePeriod(years = numberOfPeriodsAlreadyPassed))
        endPeriodDate = startPeriodDate.atEndOfPeriod(correspondingPeriod)
    } else {
        val yearStart = LocalDate(
            year = currentDate.year,
            month = startDayOfYear!!.month,
            dayOfMonth = startDayOfYear!!.dayOfMonth,
        )
        val stillFirstYear = startDate.plus(correspondingPeriod) > currentDate
        if (stillFirstYear) {
            startPeriodDate = startDate
            endPeriodDate = yearStart.atEndOfPeriod(correspondingPeriod)
        } else {
            startPeriodDate = yearStart
            endPeriodDate = startPeriodDate.atEndOfPeriod(correspondingPeriod)
        }
    }

    return startPeriodDate..endPeriodDate
}

private fun Schedule.defaultGetPeriodRange(
    currentDate: LocalDate,
): LocalDateRange {
    check(correspondingPeriod.days != 0) {
        "Corresponding period of a schedule this function operates on shouldn't have zero days " +
                "value. For weekly, monthly, or annual schedules use different functions."
    }

    var startPeriodDateIndex = startDate.daysUntil(currentDate)
    while (startPeriodDateIndex % correspondingPeriod.days == 0) {
        startPeriodDateIndex--
    }

    val startPeriodDate = startDate.plus(DatePeriod(days = startPeriodDateIndex))
    val endPeriodDate = startPeriodDate.plus(correspondingPeriod)

    return startPeriodDate..endPeriodDate
}

private fun LocalDate.atEndOfPeriod(periodToAdd: DatePeriod) =
    this.plus(periodToAdd).minus(DatePeriod(days = 1))