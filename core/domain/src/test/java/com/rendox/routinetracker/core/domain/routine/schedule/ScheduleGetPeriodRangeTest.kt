package com.rendox.routinetracker.core.domain.routine.schedule

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Test
import kotlin.random.Random

class ScheduleGetPeriodRangeTest {

    @Test
    fun weeklyScheduleGetPeriodRangeStartFromRoutineStart() {
        val routineStartDate = LocalDate(2023, Month.SEPTEMBER, 30)

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = emptyList(),
            startDayOfWeek = null,
            routineStartDate = routineStartDate,
        )

        // still first week
        val lastDayOfFirstWeek = LocalDate(2023, Month.OCTOBER, 6)
        val firstWeek = routineStartDate..lastDayOfFirstWeek

        val forthWeekStartDate = LocalDate(2023, Month.OCTOBER, 21)
        val forthWeekEndDate = LocalDate(2023, Month.OCTOBER, 27)
        val forthWeek = forthWeekStartDate..forthWeekEndDate

        assertEachDateOfRangeIsInExpectedRange(schedule, firstWeek)
        assertEachDateOfRangeIsInExpectedRange(schedule, forthWeek)
    }

    @Test
    fun weeklyScheduleGetPeriodRangeStartFromCustomDayOfWeek() {
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 18)

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = emptyList(),
            startDayOfWeek = DayOfWeek.SUNDAY,
            routineStartDate = routineStartDate,
        )

        val firstWeek = routineStartDate..LocalDate(2023, Month.OCTOBER, 21)
        assertEachDateOfRangeIsInExpectedRange(schedule, firstWeek)

        val secondWeek =
            LocalDate(2023, Month.OCTOBER, 22)..LocalDate(2023, Month.OCTOBER, 28)
        assertEachDateOfRangeIsInExpectedRange(schedule, secondWeek)
    }

    @Test
    fun monthlyScheduleGetPeriodRangeStartFromRoutineStart() {
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 18)

        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            routineStartDate = routineStartDate,
            dueDatesIndices = emptyList(),
            weekDaysMonthRelated = emptyList(),
            startFromRoutineStart = true,
        )

        val firstMonth = routineStartDate..routineStartDate
            .plus(DatePeriod(months = 1))
            .minus(DatePeriod(days = 1))
        assertEachDateOfRangeIsInExpectedRange(schedule, firstMonth)

        val secondMonthStartDay = routineStartDate.plus(DatePeriod(months = 1))
        val secondMonth = secondMonthStartDay..secondMonthStartDay
            .plus(DatePeriod(months = 1))
            .minus(DatePeriod(days = 1))
        assertEachDateOfRangeIsInExpectedRange(schedule, secondMonth)
    }

    @Test
    fun monthlyScheduleGetPeriodRangeStartFromMonthStart() {
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 18)

        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            routineStartDate = routineStartDate,
            dueDatesIndices = emptyList(),
            weekDaysMonthRelated = emptyList(),
            startFromRoutineStart = false,
        )

        val firstMonth = routineStartDate..routineStartDate.atEndOfMonth
        assertEachDateOfRangeIsInExpectedRange(schedule, firstMonth)

        val secondMonthStartDay = routineStartDate.atEndOfMonth.plus(DatePeriod(days = 1))
        val secondMonth = secondMonthStartDay..secondMonthStartDay.atEndOfMonth
        assertEachDateOfRangeIsInExpectedRange(schedule, secondMonth)
    }

    @Test
    fun annualScheduleGetPeriodRangeStartFromRoutineStartFebruary29() {
        val routineStartDate = LocalDate(2024, Month.FEBRUARY, 29)

        val schedule = Schedule.AnnualScheduleByDueDates(
            routineStartDate = routineStartDate,
            dueDates = emptyList(),
            startFromRoutineStart = true,
        )

        val firstYear = routineStartDate..LocalDate(2025, Month.FEBRUARY, 28)
        assertEachDateOfRangeIsInExpectedRange(schedule, firstYear)

        val secondYearStartDate = LocalDate(2025, Month.MARCH, 1)
        val secondYearEndDate = LocalDate(2026, Month.FEBRUARY, 28)

        val secondYear = secondYearStartDate..secondYearEndDate
        assertEachDateOfRangeIsInExpectedRange(schedule, secondYear)
    }

    @Test
    fun annualScheduleGetPeriodRangeStartFromRoutineStartMarch1() {
        val routineStartDate = LocalDate(2023, Month.MARCH, 1)

        val schedule = Schedule.AnnualScheduleByDueDates(
            routineStartDate = routineStartDate,
            dueDates = emptyList(),
            startFromRoutineStart = true,
        )

        val firstYear = routineStartDate..LocalDate(2024, Month.FEBRUARY, 29)
        assertEachDateOfRangeIsInExpectedRange(schedule, firstYear)

        val secondYearStartDate = LocalDate(2025, Month.MARCH, 1)
        val secondYearEndDate = LocalDate(2026, Month.FEBRUARY, 28)

        val secondYear = secondYearStartDate..secondYearEndDate
        assertEachDateOfRangeIsInExpectedRange(schedule, secondYear)
    }

    @Test
    fun annualScheduleGetPeriodRangeStartFromRoutineStartRandomDate() {
        val routineStartDate = LocalDate(2023, Month.SEPTEMBER, 30)

        val schedule = Schedule.AnnualScheduleByDueDates(
            routineStartDate = routineStartDate,
            dueDates = emptyList(),
            startFromRoutineStart = true,
        )

        val firstYear = routineStartDate..LocalDate(2024, Month.SEPTEMBER, 29)
        assertEachDateOfRangeIsInExpectedRange(schedule, firstYear)

        val secondYearStartDate = LocalDate(2025, Month.SEPTEMBER, 30)
        val secondYearEndDate = LocalDate(2026, Month.SEPTEMBER, 29)

        val secondYear = secondYearStartDate..secondYearEndDate
        assertEachDateOfRangeIsInExpectedRange(schedule, secondYear)
    }

    @Test
    fun annualScheduleGetPeriodRangeYearStartsFromYearStart() {
        val routineStartDate = LocalDate(2023, Month.SEPTEMBER, 30)

        val schedule = Schedule.AnnualScheduleByDueDates(
            routineStartDate = routineStartDate,
            dueDates = emptyList(),
            startFromRoutineStart = false,
        )

        val firstYear = routineStartDate..LocalDate(2023, Month.DECEMBER, 31)
        assertEachDateOfRangeIsInExpectedRange(schedule, firstYear)

        val secondYearStartDate = LocalDate(2024, Month.JANUARY, 1)
        val secondYear = secondYearStartDate..secondYearStartDate
            .plus(DatePeriod(years = 1))
            .minus(DatePeriod(days = 1))
        assertEachDateOfRangeIsInExpectedRange(schedule, secondYear)
    }

    @Test
    fun periodicCustomScheduleGetPeriodRange() {
        val routineStartDate = LocalDate(2023, Month.JULY, 6)

        val numOfDaysInPeriod = Random.nextInt(99)

        val schedule = Schedule.PeriodicCustomSchedule(
            routineStartDate = routineStartDate,
            numOfDueDays = 0,
            numOfDaysInPeriod = numOfDaysInPeriod,
        )

        val firstPeriodEndDate =
            routineStartDate.plusDays(numOfDaysInPeriod).minus(DatePeriod(days = 1))

        val firstPeriod = routineStartDate..firstPeriodEndDate
        assertEachDateOfRangeIsInExpectedRange(schedule, firstPeriod)

        val secondPeriodStartDate =
            routineStartDate.plusDays(numOfDaysInPeriod)
        val secondPeriodEndDate =
            routineStartDate.plusDays(numOfDaysInPeriod * 2).minus(DatePeriod(days = 1))

        val secondPeriod = secondPeriodStartDate..secondPeriodEndDate
        assertEachDateOfRangeIsInExpectedRange(schedule, secondPeriod)
    }

    private fun assertEachDateOfRangeIsInExpectedRange(
        schedule: Schedule.PeriodicSchedule, range: LocalDateRange
    ) {
        for (date in range) {
            assertThat(schedule.getPeriodRange(date)).isEqualTo(range)
        }
    }
}