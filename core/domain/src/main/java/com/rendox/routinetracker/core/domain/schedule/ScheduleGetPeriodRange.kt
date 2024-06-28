package com.rendox.routinetracker.core.domain.schedule

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.atEndOfYear
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

fun Schedule.PeriodicSchedule.getPeriodRange(
    currentDate: LocalDate,
    lastVacationEndDate: LocalDate? = null,
): LocalDateRange? {
    if (currentDate < startDate) {
        return null
    }

    endDate?.let {
        if (currentDate > it) {
            return null
        }
    }

    val periodRange = when (this) {
        is Schedule.WeeklyScheduleByDueDaysOfWeek -> weeklyScheduleGetPeriodDateRange(currentDate)
        is Schedule.WeeklyScheduleByNumOfDueDays -> weeklyScheduleGetPeriodDateRange(currentDate)
        is Schedule.MonthlyScheduleByDueDatesIndices -> monthlyScheduleGetPeriodDateRange(currentDate)
        is Schedule.MonthlyScheduleByNumOfDueDays -> monthlyScheduleGetPeriodDateRange(currentDate)
        is Schedule.AnnualScheduleByDueDates -> annualScheduleGetPeriodDateRange(currentDate)
        is Schedule.AnnualScheduleByNumOfDueDays -> annualScheduleGetPeriodDateRange(currentDate)
        is Schedule.AlternateDaysSchedule -> alternateDaysScheduleGetPeriodRange(
            currentDate, lastVacationEndDate
        )
    }

    endDate?.let {
        if (periodRange.endInclusive > it) {
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
        val numberOfPeriodsAlreadyPassed = startDate.until(currentDate, unit)
        startPeriodDate = startDate.plus(numberOfPeriodsAlreadyPassed, unit)
        endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)
    } else {
        startPeriodDate = currentDate
        while (startPeriodDate.dayOfWeek != startDayOfWeek && startPeriodDate != startDate) {
            startPeriodDate = startPeriodDate.minus(DatePeriod(days = 1))
        }
        val endDateDayOfWeekIndex = startDayOfWeek!!.value - 1
        val endDateDayOfWeek = if (endDateDayOfWeekIndex == 0) {
            DayOfWeek.SUNDAY
        } else {
            DayOfWeek(endDateDayOfWeekIndex)
        }
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

    if (startFromHabitStart) {
        val numberOfPeriodsAlreadyPassed = startDate.monthsUntil(currentDate)
        startPeriodDate = startDate.plus(DatePeriod(months = numberOfPeriodsAlreadyPassed))
        endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)
    } else {
        val stillFirstMonth = currentDate <= startDate.atEndOfMonth
        startPeriodDate = if (stillFirstMonth) {
            startDate
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

    if (startFromHabitStart) {
        val numberOfPeriodsAlreadyPassed = startDate.yearsUntil(currentDate)
        startPeriodDate = startDate.plus(DatePeriod(years = numberOfPeriodsAlreadyPassed))
        endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)

        // plus and until functions don't work as expected with february 29
        if (startDate.month == Month.FEBRUARY && startDate.dayOfMonth == 29) {
            if (numberOfPeriodsAlreadyPassed > 0) {
                startPeriodDate = startPeriodDate.plusDays(1)
            }
            endPeriodDate = endPeriodDate.plusDays(1)
        }
    } else {
        val stillFirstYear = currentDate <= startDate.atEndOfYear
        startPeriodDate = if (stillFirstYear) {
            startDate
        } else {
            LocalDate(currentDate.year, Month.JANUARY, 1)
        }
        endPeriodDate = startPeriodDate.atEndOfYear
    }
    return startPeriodDate..endPeriodDate
}

private fun Schedule.AlternateDaysSchedule.alternateDaysScheduleGetPeriodRange(
    currentDate: LocalDate,
    lastVacationEndDate: LocalDate?,
    ): LocalDateRange {
    check(correspondingPeriod.days != 0) {
        "Corresponding period of a schedule this function operates on shouldn't have zero days " +
                "value. For weekly, monthly, or annual schedules use different functions."
    }

    // when vacation ends, the user will expect the period to be restarted
    val scheduleStartDate = if (lastVacationEndDate != null && lastVacationEndDate <= currentDate) {
        lastVacationEndDate.plusDays(1)
    } else {
        startDate
    }
    var startPeriodDateIndex = scheduleStartDate.daysUntil(currentDate)
    while (startPeriodDateIndex % correspondingPeriod.days != 0) {
        startPeriodDateIndex--
    }

    val startPeriodDate = scheduleStartDate.plusDays(startPeriodDateIndex)
    val endPeriodDate = atEndOfPeriod(startPeriodDate, correspondingPeriod)

    return startPeriodDate..endPeriodDate
}

private fun atEndOfPeriod(startPeriodDate: LocalDate, periodToAdd: DatePeriod) =
    startPeriodDate.plus(periodToAdd).minus(DatePeriod(days = 1))