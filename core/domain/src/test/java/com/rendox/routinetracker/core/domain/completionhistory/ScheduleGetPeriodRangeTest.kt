package com.rendox.routinetracker.core.domain.completionhistory

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.domain.schedule.getPeriodRange
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Schedule
import kotlin.random.Random
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class ScheduleGetPeriodRangeTest {

    @Test
    fun weeklyScheduleGetPeriodRangeStartFromRoutineStart() {
        val routineStartDate = LocalDate(2023, Month.SEPTEMBER, 30)

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = emptyList(),
            startDayOfWeek = null,
            startDate = routineStartDate,
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
            startDate = routineStartDate,
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
            startDate = routineStartDate,
            dueDatesIndices = emptyList(),
            weekDaysMonthRelated = emptyList(),
            startFromHabitStart = true,
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
            startDate = routineStartDate,
            dueDatesIndices = emptyList(),
            weekDaysMonthRelated = emptyList(),
            startFromHabitStart = false,
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
            startDate = routineStartDate,
            dueDates = emptyList(),
            startFromHabitStart = true,
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
            startDate = routineStartDate,
            dueDates = emptyList(),
            startFromHabitStart = true,
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
            startDate = routineStartDate,
            dueDates = emptyList(),
            startFromHabitStart = true,
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
            startDate = routineStartDate,
            dueDates = emptyList(),
            startFromHabitStart = false,
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
    fun alternateDaysScheduleGetPeriodRange() {
        val routineStartDate = LocalDate(2023, Month.JULY, 6)

        val numOfDaysInPeriod = Random.nextInt(99)

        val schedule = Schedule.AlternateDaysSchedule(
            startDate = routineStartDate,
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

    @ParameterizedTest
    @CsvSource(
        "2024-01-30, 2024-01-30, 2024-01-30, 2024-02-28",
        "2024-01-30, 2024-03-01, 2024-02-29, 2024-03-29",
        "2024-01-30, 2024-02-01, 2024-01-30, 2024-02-28",
        "2024-01-30, 2024-02-29, 2024-02-29, 2024-03-29",
        "2023-01-30, 2023-02-28, 2023-02-28, 2023-03-29",
        "2024-01-31, 2024-02-01, 2024-01-31, 2024-02-28",
        "2024-01-31, 2024-02-29, 2024-02-29, 2024-03-30",
        "2024-01-31, 2024-04-30, 2024-04-30, 2024-05-30",
        "2024-01-31, 2024-03-01, 2024-02-29, 2024-03-30",
        "2024-01-31, 2024-04-01, 2024-03-31, 2024-04-29",
        "2024-01-29, 2024-02-29, 2024-02-29, 2024-03-28",
        "2023-01-29, 2023-02-28, 2023-02-28, 2023-03-28",
        "2023-01-29, 2023-03-01, 2023-02-28, 2023-03-28",
    )
    fun `monthly schedule range is correct for inconsistent dates`(
        startDate: String,
        currentDate: String,
        periodStart: String,
        periodEnd: String,
    ) {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            startDate = startDate.toLocalDate(),
            numOfDueDays = 2,
        )
        val periodRange = schedule.getPeriodRange(currentDate.toLocalDate())
        val expectedPeriod = periodStart.toLocalDate()..periodEnd.toLocalDate()
        assertThat(periodRange).isEqualTo(expectedPeriod)
    }

    @Test
    fun periodRangeIsCorrectForWholeYear() {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            startDate = LocalDate(2024, 1, 30),
            numOfDueDays = 2,
        )
        val periods = mutableListOf<LocalDateRange>()
        for (date in schedule.startDate..schedule.startDate.plus(DatePeriod(years = 1))) {
            periods.add(schedule.getPeriodRange(date))
        }
        println("periods = ${periods.distinct()}")
    }

    @ParameterizedTest
    @ValueSource(strings = ["2023-09-30", "2023-10-01"])
    fun `monthly schedule range is correct for end of month dates`(currentDate: String) {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            startDate = LocalDate(2023, 7, 31),
            numOfDueDays = 2,
        )
        val expectedRange = LocalDateRange(
            start = LocalDate(2023, 9, 30),
            endInclusive = LocalDate(2023, 10, 29),
        )
        assertThat(schedule.getPeriodRange(currentDate.toLocalDate())).isEqualTo(expectedRange)
    }

    private fun assertEachDateOfRangeIsInExpectedRange(
        schedule: Schedule.PeriodicSchedule,
        range: LocalDateRange,
    ) {
        for (date in range) {
            assertThat(schedule.getPeriodRange(date)).isEqualTo(range)
        }
    }
}