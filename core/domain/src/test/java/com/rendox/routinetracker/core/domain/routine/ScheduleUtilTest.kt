package com.rendox.routinetracker.core.domain.routine

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.logic.time.WeekDayNumberMonthRelated
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
import kotlin.random.nextInt

class ScheduleUtilTest {

    private val routineStartDate = LocalDate(2023, Month.JANUARY, 1)

    @Test
    fun everyDayScheduleIsDue() {
        val schedule: Schedule = Schedule.EveryDaySchedule(
            routineStartDate = routineStartDate,
        )
        val date1 = LocalDate(2023, (1..12).random(), (1..28).random())
        val date2 = LocalDate(2024, Month.FEBRUARY, 29)
        val date3 = LocalDate(2022, Month.OCTOBER, 31)
        assertThat(schedule.isDue(date1)).isTrue()
        assertThat(schedule.isDue(date2)).isTrue()
        assertThat(schedule.isDue(date3)).isTrue()
    }

    @Test
    fun weeklyScheduleIsDue() {
        val dueDaysOfWeek1 = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.SUNDAY,
        )

        val dueDaysOfWeek2 = listOf(
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
        )
        val schedule1: Schedule = Schedule.WeeklySchedule(
            routineStartDate = routineStartDate,
            dueDaysOfWeek = dueDaysOfWeek1,
        )
        val schedule2: Schedule = Schedule.WeeklySchedule(
            routineStartDate = routineStartDate,
            dueDaysOfWeek = dueDaysOfWeek2,
        )

        val monday = LocalDate(2023, Month.OCTOBER, 30)
        val tuesday = LocalDate(2023, Month.DECEMBER, 12)
        val wednesday = LocalDate(2024, Month.JANUARY, 3)
        val thursday = LocalDate(2024, Month.FEBRUARY, 29)
        val friday = LocalDate(2024, Month.JUNE, 7)
        val saturday = LocalDate(2024, Month.JUNE, 15)
        val sunday = LocalDate(2024, Month.JUNE, 30)

        assertThat(schedule1.isDue(monday)).isTrue()
        assertThat(schedule1.isDue(wednesday)).isTrue()
        assertThat(schedule1.isDue(thursday)).isTrue()
        assertThat(schedule1.isDue(sunday)).isTrue()
        assertThat(schedule1.isDue(tuesday)).isFalse()
        assertThat(schedule1.isDue(friday)).isFalse()
        assertThat(schedule1.isDue(saturday)).isFalse()

        assertThat(schedule2.isDue(tuesday)).isTrue()
        assertThat(schedule2.isDue(wednesday)).isTrue()
        assertThat(schedule2.isDue(friday)).isTrue()
        assertThat(schedule2.isDue(saturday)).isTrue()
        assertThat(schedule2.isDue(monday)).isFalse()
        assertThat(schedule2.isDue(thursday)).isFalse()
        assertThat(schedule2.isDue(sunday)).isFalse()
    }

    @Test
    fun `MonthlySchedule is due, check due dates`() {
        val dates = (1..28).shuffled()
        val dueDatesIndices = dates.take(14)
        val dueDates = dueDatesIndices.map { dueDateIndex ->
            LocalDate(
                year = 2023,
                monthNumber = Random.nextInt(1..12),
                dayOfMonth = dueDateIndex,
            )
        }
        val notDueDatesIndices = dates.drop(14)
        val notDueDates = notDueDatesIndices.map { notDueDateIndex ->
            LocalDate(
                year = 2023,
                monthNumber = Random.nextInt(1..12),
                dayOfMonth = notDueDateIndex,
            )
        }

        val schedule: Schedule = Schedule.MonthlySchedule(
            dueDatesIndices = dueDatesIndices,
            includeLastDayOfMonth = false,
            startFromRoutineStart = true,
            weekDaysMonthRelated = emptyList(),
            routineStartDate = routineStartDate,
        )

        for (dueDate in dueDates) {
            assertThat(schedule.isDue(dueDate)).isTrue()
        }

        for (notDueDate in notDueDates) {
            assertThat(schedule.isDue(notDueDate)).isFalse()
        }
    }

    @Test
    fun `MonthlySchedule is due, check last day of month`() {
        val februaryHeapYearLastDate = LocalDate(2024, Month.FEBRUARY, 29)
        val februaryHeapYearDayBeforeLast = februaryHeapYearLastDate.minus(DatePeriod(days = 1))
        val februaryLastDate = LocalDate(2023, Month.FEBRUARY, 28)
        val januaryLastDate = LocalDate(2024, Month.JANUARY, 31)
        val aprilLastDate = LocalDate(2024, Month.APRIL, 30)
        val januaryDayBeforeLast = januaryLastDate.minus(DatePeriod(days = 1))
        val aprilDayBeforeLast = aprilLastDate.minus(DatePeriod(days = 1))

        val schedule = Schedule.MonthlySchedule(
            dueDatesIndices = emptyList(),
            includeLastDayOfMonth = true,
            startFromRoutineStart = true,
            weekDaysMonthRelated = emptyList(),
            routineStartDate = routineStartDate,
        )

        assertThat(schedule.isDue(februaryHeapYearLastDate)).isTrue()
        assertThat(schedule.isDue(februaryLastDate)).isTrue()
        assertThat(schedule.isDue(januaryLastDate)).isTrue()
        assertThat(schedule.isDue(aprilLastDate)).isTrue()
        assertThat(schedule.isDue(februaryHeapYearDayBeforeLast)).isFalse()
        assertThat(schedule.isDue(januaryDayBeforeLast)).isFalse()
        assertThat(schedule.isDue(aprilDayBeforeLast)).isFalse()
    }

    @Test
    fun `MonthlySchedule is due, check WeekDayMonthRelated`() {
        val schedule: Schedule = Schedule.MonthlySchedule(
            dueDatesIndices = emptyList(),
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = listOf(
                WeekDayMonthRelated(DayOfWeek.MONDAY, WeekDayNumberMonthRelated.First),
                WeekDayMonthRelated(DayOfWeek.TUESDAY, WeekDayNumberMonthRelated.Third),
                WeekDayMonthRelated(DayOfWeek.FRIDAY, WeekDayNumberMonthRelated.Second),
                WeekDayMonthRelated(DayOfWeek.MONDAY, WeekDayNumberMonthRelated.Forth),
                WeekDayMonthRelated(DayOfWeek.SUNDAY, WeekDayNumberMonthRelated.Last),
                WeekDayMonthRelated(DayOfWeek.WEDNESDAY, WeekDayNumberMonthRelated.Forth),
                WeekDayMonthRelated(DayOfWeek.THURSDAY, WeekDayNumberMonthRelated.Fifth),
            ),
            startFromRoutineStart = true,
            routineStartDate = routineStartDate,
        )

        val firstMonday1 = LocalDate(2024, Month.FEBRUARY, 5)
        val firstMonday2 = LocalDate(2024, Month.APRIL, 1)
        val thirdTuesday = LocalDate(2024, Month.JUNE, 18)
        val secondFriday = LocalDate(2024, Month.MARCH, 8)
        val forthMonday = LocalDate(2024, Month.JANUARY, 22)
        val lastSundayAndLastDayOfMonth = LocalDate(2023, Month.DECEMBER, 31)
        val forthAndLastSunday = LocalDate(2024, Month.JANUARY, 28)
        val forthNotLastSunday = LocalDate(2024, Month.MARCH, 24)
        val fifthThursday = LocalDate(2024, Month.MAY, 30)
        val forthThursday = LocalDate(2024, Month.MAY, 23)
        val forthWednesday = LocalDate(2023, Month.NOVEMBER, 22)
        val secondTuesday = LocalDate(2023, Month.NOVEMBER, 14)

        assertThat(schedule.isDue(firstMonday1)).isTrue()
        assertThat(schedule.isDue(firstMonday2)).isTrue()
        assertThat(schedule.isDue(thirdTuesday)).isTrue()
        assertThat(schedule.isDue(secondFriday)).isTrue()
        assertThat(schedule.isDue(forthMonday)).isTrue()
        assertThat(schedule.isDue(lastSundayAndLastDayOfMonth)).isTrue()
        assertThat(schedule.isDue(forthAndLastSunday)).isTrue()
        assertThat(schedule.isDue(forthNotLastSunday)).isFalse()
        assertThat(schedule.isDue(fifthThursday)).isTrue()
        assertThat(schedule.isDue(forthThursday)).isFalse()
        assertThat(schedule.isDue(forthWednesday)).isTrue()
        assertThat(schedule.isDue(secondTuesday)).isFalse()
    }

    @Test
    fun periodicCustomScheduleIsDue() {
        val numOfDaysInPeriod = Random.nextInt(2..100)

        val daysIndices = (1..numOfDaysInPeriod).shuffled()
        val dueDaysNumber = numOfDaysInPeriod / 2
        val dueDaysIndices = daysIndices.take(dueDaysNumber)
        val notDueDaysIndices = daysIndices.drop(dueDaysNumber)

        val schedule: Schedule = Schedule.PeriodicCustomSchedule(
            dueDatesIndices = dueDaysIndices,
            numOfDaysInPeriod = numOfDaysInPeriod,
            routineStartDate = routineStartDate,
        )

        for (dueDayIndex in dueDaysIndices) {
            val numOfPeriodsAlreadyPassed = Random.nextInt(1..50)
            assertThat(
                schedule.isDue(
                    validationDate = routineStartDate.plus(
                        DatePeriod(
                            // subtract one because indices start count from 1, not from 0
                            days = numOfDaysInPeriod * numOfPeriodsAlreadyPassed + dueDayIndex - 1
                        )
                    ),
                )
            ).isTrue()
        }

        for (notDueDayIndex in notDueDaysIndices) {
            val numOfPeriodsAlreadyPassed = Random.nextInt(1..50)
            assertThat(
                schedule.isDue(
                    validationDate = routineStartDate.plus(
                        DatePeriod(
                            // subtract one because indices start count from 1, not from 0
                            days = numOfDaysInPeriod * numOfPeriodsAlreadyPassed + notDueDayIndex - 1
                        )
                    ),
                )
            ).isFalse()
        }
    }

    @Test
    fun customDateScheduleIsDue() {
        val dueDates = mutableListOf<LocalDate>()
        val notDueDates = mutableListOf<LocalDate>()

        val monthNumbers = (1..12).shuffled()
        val dueDatesMonthNumbers = monthNumbers.take(6)
        val notDueDatesMonthNumbers = monthNumbers.drop(6)

        val daysOfMonth = (1..28).shuffled()
        val dueDaysOfMonth = daysOfMonth.take(14)
        val notDueDaysOfMonth = daysOfMonth.drop(14)

        repeat(30) {
            dueDates.add(
                LocalDate(
                    year = 2024,
                    monthNumber = dueDatesMonthNumbers.random(),
                    dayOfMonth = dueDaysOfMonth.random(),
                )
            )
        }

        repeat(30) {
            notDueDates.add(
                LocalDate(
                    year = 2024,
                    monthNumber = notDueDatesMonthNumbers.random(),
                    dayOfMonth = notDueDaysOfMonth.random(),
                )
            )
        }

        dueDates.add(LocalDate(2024, Month.FEBRUARY, 29))
        dueDates.add(LocalDate(2024, Month.JANUARY, 31))
        dueDates.add(LocalDate(2024, Month.JUNE, 30))

        notDueDates.add(LocalDate(2024, Month.JANUARY, 30))
        notDueDates.add(LocalDate(2024, Month.AUGUST, 31))
        notDueDates.add(LocalDate(2024, Month.SEPTEMBER, 30))

        val schedule = Schedule.CustomDateSchedule(
            routineStartDate = routineStartDate,
            dueDates = dueDates,
        )

        for (dueDate in dueDates) {
            assertThat(schedule.isDue(dueDate)).isTrue()
        }

        for (notDueDate in notDueDates) {
            assertThat(schedule.isDue(notDueDate)).isFalse()
        }
    }

    @Test
    fun annualScheduleIsDue() {
        val dueDates = mutableListOf<AnnualDate>()
        val notDueDates = mutableListOf<AnnualDate>()

        val monthNumbers = (1..12).shuffled()
        val dueDatesMonthNumbers = monthNumbers.take(6)
        val notDueDatesMonthNumbers = monthNumbers.drop(6)

        val daysOfMonth = (1..27).shuffled()
        val dueDaysOfMonth = daysOfMonth.take(14)
        val notDueDaysOfMonth = daysOfMonth.drop(14)

        repeat(30) {
            dueDates.add(
                AnnualDate(
                    month = Month(dueDatesMonthNumbers.random()),
                    dayOfMonth = dueDaysOfMonth.random(),
                )
            )
        }

        repeat(30) {
            notDueDates.add(
                AnnualDate(
                    month = Month(notDueDatesMonthNumbers.random()),
                    dayOfMonth = notDueDaysOfMonth.random(),
                )
            )
        }

        val expectedDueDates = mutableListOf<LocalDate>()

        dueDates.forEach {
            expectedDueDates.add(
                LocalDate(
                    year = Random.nextInt(2024..2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth
                )
            )
            expectedDueDates.add(
                LocalDate(
                    year = Random.nextInt(2024..2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth,
                )
            )
        }

        val expectedNotDueDates = mutableListOf<LocalDate>()

        notDueDates.forEach {
            expectedNotDueDates.add(
                LocalDate(
                    year = Random.nextInt(2024..2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth
                )
            )
            expectedNotDueDates.add(
                LocalDate(
                    year = Random.nextInt(2024..2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth,
                )
            )
        }

        dueDates.add(AnnualDate(Month.FEBRUARY, 29))
        expectedDueDates.add(LocalDate(2024, Month.FEBRUARY, 29))
        expectedNotDueDates.add(LocalDate(2025, Month.FEBRUARY, 28))

        val schedule = Schedule.AnnualSchedule(
            dueDates = dueDates,
            routineStartDate = routineStartDate,
            startFromRoutineStart = false,
        )

        for (dueDate in expectedDueDates) {
            assertThat(schedule.isDue(dueDate)).isTrue()
        }

        for (notDueDate in expectedNotDueDates) {
            assertThat(schedule.isDue(notDueDate)).isFalse()
        }
    }

    @Test
    fun weeklyScheduleGetPeriodRangeStartFromRoutineStart() {
        val routineStartDate = LocalDate(2023, Month.SEPTEMBER, 30)

        val schedule = Schedule.WeeklySchedule(
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

        val schedule = Schedule.WeeklySchedule(
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

        val schedule = Schedule.MonthlySchedule(
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

        val schedule = Schedule.MonthlySchedule(
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

        val schedule = Schedule.AnnualSchedule(
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

        val schedule = Schedule.AnnualSchedule(
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

        val schedule = Schedule.AnnualSchedule(
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

        val schedule = Schedule.AnnualSchedule(
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
    fun periodicCustomScheduleTest() {
        val routineStartDate = LocalDate(2023, Month.JULY, 6)

        val numOfDaysInPeriod = Random.nextInt(99)

        val schedule = Schedule.PeriodicCustomSchedule(
            routineStartDate = routineStartDate,
            dueDatesIndices = emptyList(),
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

    private fun assertEachDateOfRangeIsInExpectedRange(schedule: Schedule, range: LocalDateRange) {
        for (date in range) {
            assertThat(schedule.getPeriodRange(date)).isEqualTo(range)
        }
    }
}