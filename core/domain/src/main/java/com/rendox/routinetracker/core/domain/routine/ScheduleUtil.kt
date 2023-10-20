package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.atEndOfYear
import com.rendox.routinetracker.core.logic.time.matches
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.logic.time.withDayOfMonth
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
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
    is Schedule.PeriodicCustomSchedule -> periodicCustomScheduleIsDue(validationDate, routineStartDate)
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
    val periodStartDate = routineStartDate.plusDays(daysCounter)
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

fun Schedule.getPeriodRange(currentDate: LocalDate): LocalDateRange? {
    val periodRange = when (this) {
        is Schedule.EveryDaySchedule -> null
        is Schedule.WeeklySchedule -> weeklyScheduleGetPeriodDateRange(currentDate)
        is Schedule.MonthlySchedule -> monthlyScheduleGetPeriodDateRange(currentDate)
        is Schedule.AnnualSchedule -> annualScheduleGetPeriodDateRange(currentDate)
        is Schedule.PeriodicCustomSchedule -> periodicCustomScheduleGetPeriodDateRange(currentDate)
        is Schedule.CustomDateSchedule -> null
    }
    routineEndDate?.let {
        if (periodRange != null && periodRange.endInclusive > it) {
            return periodRange.copy(endInclusive = it)
        }
    }

    return periodRange
}

private fun Schedule.WeeklySchedule.weeklyScheduleGetPeriodDateRange(
    currentDate: LocalDate,
): LocalDateRange {
    var startPeriodDate: LocalDate
    var endPeriodDate: LocalDate

    val startFromRoutineStart = startDayOfWeek == null
    if (startFromRoutineStart) {
        val unit = DateTimeUnit.WEEK
        val numberOfPeriodsAlreadyPassed = routineStartDate.until(currentDate, unit)
        startPeriodDate = routineStartDate.plus(numberOfPeriodsAlreadyPassed, unit)
        endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)
    } else {
        startPeriodDate = currentDate
        while (startPeriodDate.dayOfWeek != startDayOfWeek && startPeriodDate != routineStartDate) {
            startPeriodDate = startPeriodDate.minus(DatePeriod(days = 1))
        }
        val endDateDayOfWeek = DayOfWeek(startDayOfWeek!!.value - 1)
        endPeriodDate = currentDate
        while (endPeriodDate.dayOfWeek != endDateDayOfWeek) {
            endPeriodDate = endPeriodDate.plusDays(1)
        }
    }
    return startPeriodDate..endPeriodDate
}

private fun Schedule.MonthlySchedule.monthlyScheduleGetPeriodDateRange(
    currentDate: LocalDate,
): LocalDateRange {
    val startPeriodDate: LocalDate
    val endPeriodDate: LocalDate

    if (startFromRoutineStart) {
        val numberOfPeriodsAlreadyPassed = routineStartDate.monthsUntil(currentDate)
        startPeriodDate = routineStartDate.plus(DatePeriod(months = numberOfPeriodsAlreadyPassed))
        endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)
    } else {
        val stillFirstMonth = currentDate <= routineStartDate.atEndOfMonth
        startPeriodDate = if (stillFirstMonth) {
            routineStartDate
        } else {
            currentDate.withDayOfMonth(1)
        }
        endPeriodDate = startPeriodDate.atEndOfMonth
    }
    return startPeriodDate..endPeriodDate
}

private fun Schedule.AnnualSchedule.annualScheduleGetPeriodDateRange(
    currentDate: LocalDate,
): LocalDateRange {
    var startPeriodDate: LocalDate
    var endPeriodDate: LocalDate

    if (startFromRoutineStart) {
        val numberOfPeriodsAlreadyPassed = routineStartDate.yearsUntil(currentDate)
        startPeriodDate = routineStartDate.plus(DatePeriod(years = numberOfPeriodsAlreadyPassed))
        endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)

        // plus and until functions don't work as expected with february 29
        if (routineStartDate.month == Month.FEBRUARY && routineStartDate.dayOfMonth == 29) {
            if (numberOfPeriodsAlreadyPassed > 0) {
                startPeriodDate = startPeriodDate.plusDays(1)
            }
            endPeriodDate = endPeriodDate.plusDays(1)
        }
    } else {
        val stillFirstYear = currentDate <= routineStartDate.atEndOfYear
        startPeriodDate = if (stillFirstYear) {
            routineStartDate
        } else {
            LocalDate(currentDate.year, Month.JANUARY, 1)
        }
        endPeriodDate = startPeriodDate.atEndOfYear
    }
    return startPeriodDate..endPeriodDate
}

private fun Schedule.PeriodicCustomSchedule.periodicCustomScheduleGetPeriodDateRange(
    currentDate: LocalDate,
): LocalDateRange {
    check(correspondingPeriod.days != 0) {
        "Corresponding period of a schedule this function operates on shouldn't have zero days " +
                "value. For weekly, monthly, or annual schedules use different functions."
    }

    var startPeriodDateIndex = routineStartDate.daysUntil(currentDate)
    while (startPeriodDateIndex % correspondingPeriod.days != 0) {
        startPeriodDateIndex--
    }

    val startPeriodDate = routineStartDate.plusDays(startPeriodDateIndex)
    val endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)

    return startPeriodDate..endPeriodDate
}

private fun atEndOfPeriod(startPeriodDate: LocalDate, periodToAdd: DatePeriod) =
    startPeriodDate.plus(periodToAdd).minus(DatePeriod(days = 1))