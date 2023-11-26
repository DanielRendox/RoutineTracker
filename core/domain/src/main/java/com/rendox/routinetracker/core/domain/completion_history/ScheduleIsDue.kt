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
    scheduleDeviationInCurrentPeriod: Double? = null,
    lastVacationEndDate: LocalDate? = null,
): Boolean {
    println("check if schedule is due on $validationDate")
    if (validationDate < routineStartDate) return false
    routineEndDate?.let { if (validationDate > it) return false }

    println("check if schedule is due 2 on $validationDate")


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
                scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod ?: 0.0,
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
                scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod ?: 0.0,
            )
        }

        is Schedule.PeriodicCustomSchedule -> {
            val period = getPeriodRange(validationDate, lastVacationEndDate)
            scheduleByNumOfDueDaysIsDue(
                validationDate = validationDate,
                validationDatePeriod = period,
                numOfDueDays = getNumOfDueDatesInPeriod(period),
                numOfCompletedDays = numOfTimesCompletedInCurrentPeriod,
                dateScheduleDeviationIsActualFor = actualDate,
                scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod ?: 0.0,
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
                scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod ?: 0.0,
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
    scheduleDeviationInCurrentPeriod: Double,
): Boolean {
    val validationDateNumberInPeriod = validationDatePeriod.start.daysUntil(validationDate) + 1
    println("validation date = $validationDate")
    println("validation date period = $validationDatePeriod")
    println("validationDateNumberInPeriod = $validationDateNumberInPeriod")
    println("num of due days = $numOfDueDays")
    if (validationDateNumberInPeriod <= numOfDueDays) return true

    println("dateScheduleDeviationIsActualFor = $dateScheduleDeviationIsActualFor")

    dateScheduleDeviationIsActualFor?.let {
        val daysBetweenLastDateAndValidationDate = it.daysUntil(validationDate) - 1
        val nonNegativeScheduleDeviation =
            if (scheduleDeviationInCurrentPeriod < 0) -scheduleDeviationInCurrentPeriod else 0.0
        val daysExpectedToBeCompletedTillThatTime =
            numOfCompletedDays + nonNegativeScheduleDeviation + daysBetweenLastDateAndValidationDate.toDouble()
        println("validationDate = $validationDate")
        println("numOfCompletedDays = $numOfCompletedDays")
        println("nonNegativeScheduleDeviation = $nonNegativeScheduleDeviation")
        println("daysBetweenLastDateAndValidationDate = $daysBetweenLastDateAndValidationDate")
        println()
        if (daysExpectedToBeCompletedTillThatTime < (numOfDueDays.toDouble())) return true
    }

    return false
}