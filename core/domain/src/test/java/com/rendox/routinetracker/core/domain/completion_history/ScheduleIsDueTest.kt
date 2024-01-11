package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.logic.time.WeekDayNumberMonthRelated
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertFailsWith

class ScheduleIsDueTest {

    private val routineStartDate = LocalDate(2023, Month.JANUARY, 1)

    @Test
    fun everyDayScheduleIsDue() {
        val schedule: Schedule = Schedule.EveryDaySchedule(
            startDate = routineStartDate,
        )
        val date1 = LocalDate(2023, (1..12).random(), (1..28).random())
        val date2 = LocalDate(2024, Month.FEBRUARY, 29)
        assertThat(schedule.isDue(validationDate = date1)).isTrue()
        assertThat(schedule.isDue(validationDate = date2)).isTrue()
    }

    @Test
    fun weeklyScheduleByDueDaysIsDue() {
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
        val schedule1: Schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            startDate = routineStartDate,
            dueDaysOfWeek = dueDaysOfWeek1,
        )
        val schedule2: Schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            startDate = routineStartDate,
            dueDaysOfWeek = dueDaysOfWeek2,
        )

        val monday = LocalDate(2023, Month.OCTOBER, 30)
        val tuesday = LocalDate(2023, Month.DECEMBER, 12)
        val wednesday = LocalDate(2024, Month.JANUARY, 3)
        val thursday = LocalDate(2024, Month.FEBRUARY, 29)
        val friday = LocalDate(2024, Month.JUNE, 7)
        val saturday = LocalDate(2024, Month.JUNE, 15)
        val sunday = LocalDate(2024, Month.JUNE, 30)

        assertThat(schedule1.isDue(monday, null)).isTrue()
        assertThat(schedule1.isDue(wednesday, null)).isTrue()
        assertThat(schedule1.isDue(thursday, null)).isTrue()
        assertThat(schedule1.isDue(sunday, null)).isTrue()
        assertThat(schedule1.isDue(tuesday, null)).isFalse()
        assertThat(schedule1.isDue(friday, null)).isFalse()
        assertThat(schedule1.isDue(saturday, null)).isFalse()

        assertThat(schedule2.isDue(tuesday, null)).isTrue()
        assertThat(schedule2.isDue(wednesday, null)).isTrue()
        assertThat(schedule2.isDue(friday, null)).isTrue()
        assertThat(schedule2.isDue(saturday, null)).isTrue()
        assertThat(schedule2.isDue(monday, null)).isFalse()
        assertThat(schedule2.isDue(thursday, null)).isFalse()
        assertThat(schedule2.isDue(sunday, null)).isFalse()
    }

    @Test
    fun `weekly schedule, first period is short, assert fails with an exception`() {
        assertFailsWith<IllegalStateException> {
            Schedule.WeeklyScheduleByNumOfDueDays(
                startDate = routineStartDate,
                numOfDueDays = 5,
                startDayOfWeek = DayOfWeek.MONDAY,
                numOfDueDaysInFirstPeriod = null,
            )
        }
    }

    @Test
    fun `weekly schedule due X days per week, future dates, due on first X days`() {
        val numOfDueDays = 5

        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            startDate = routineStartDate,
            numOfDueDays = numOfDueDays,
            startDayOfWeek = null,
            numOfDueDaysInFirstPeriod = null,
        )

        val futurePeriodStart = schedule.startDate.plusDays(DateTimeUnit.WEEK.days)
        for (dayIndex in 0 until numOfDueDays) {
            assertThat(
                schedule.isDue(validationDate = futurePeriodStart.plusDays(dayIndex))
            ).isTrue()
        }

        for (dayIndex in numOfDueDays until DateTimeUnit.WEEK.days) {
            assertThat(
                schedule.isDue(validationDate = futurePeriodStart.plusDays(dayIndex))
            ).isFalse()
        }
    }

    @Test
    fun `weekly schedule due X days per week, assert works fine when first period is short`() {
        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            startDate = LocalDate(2023, Month.NOVEMBER, 1), // wednesday
            numOfDueDays = 4,
            startDayOfWeek = DayOfWeek.MONDAY,
            numOfDueDaysInFirstPeriod = 3,
        )

        for (dayIndex in 0..2) {
            val currentDate = schedule.startDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = currentDate)
            ).isTrue()
        }

        for (dayIndex in 3..4) {
            val currentDate = schedule.startDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = currentDate)
            ).isFalse()
        }

        for (dayIndex in 5..8) {
            val currentDate = schedule.startDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = currentDate)
            ).isTrue()
        }

        for (dayIndex in 9..11) {
            val currentDate = schedule.startDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = currentDate)
            ).isFalse()
        }

        for (dayIndex in 12..15) {
            val currentDate = schedule.startDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = currentDate)
            ).isTrue()
        }

        for (dayIndex in 16..18) {
            val currentDate = schedule.startDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = currentDate)
            ).isFalse()
        }
    }

    @Test
    fun `MonthlySchedule is due, check due dates`() {
        val dates = (1..28).shuffled()
        val dueDatesIndices = dates.take(14)
        val dueDates = dueDatesIndices.map { dueDateIndex ->
            LocalDate(
                year = 2023,
                monthNumber = Random.nextInt(1, 12),
                dayOfMonth = dueDateIndex,
            )
        }
        val notDueDatesIndices = dates.drop(14)
        val notDueDates = notDueDatesIndices.map { notDueDateIndex ->
            LocalDate(
                year = 2023,
                monthNumber = Random.nextInt(1, 12),
                dayOfMonth = notDueDateIndex,
            )
        }

        val schedule: Schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = dueDatesIndices,
            includeLastDayOfMonth = false,
            startFromHabitStart = true,
            weekDaysMonthRelated = emptyList(),
            startDate = routineStartDate,
        )

        for (dueDate in dueDates) {
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isTrue()
        }

        for (notDueDate in notDueDates) {
            assertThat(
                schedule.isDue(validationDate = notDueDate)
            ).isFalse()
        }
    }

    @Test
    fun `MonthlySchedule is due, last day of month is due, assert works correctly`() {
        val februaryHeapYearLastDate = LocalDate(2024, Month.FEBRUARY, 29)
        val februaryHeapYearDayBeforeLast = februaryHeapYearLastDate.minus(DatePeriod(days = 1))
        val februaryLastDate = LocalDate(2023, Month.FEBRUARY, 28)
        val januaryLastDate = LocalDate(2024, Month.JANUARY, 31)
        val aprilLastDate = LocalDate(2024, Month.APRIL, 30)
        val januaryDayBeforeLast = januaryLastDate.minus(DatePeriod(days = 1))
        val aprilDayBeforeLast = aprilLastDate.minus(DatePeriod(days = 1))

        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = emptyList(),
            includeLastDayOfMonth = true,
            startFromHabitStart = true,
            weekDaysMonthRelated = emptyList(),
            startDate = routineStartDate,
        )

        assertThat(schedule.isDue(februaryHeapYearLastDate, null)).isTrue()
        assertThat(schedule.isDue(februaryLastDate, null)).isTrue()
        assertThat(schedule.isDue(januaryLastDate, null)).isTrue()
        assertThat(schedule.isDue(aprilLastDate, null)).isTrue()
        assertThat(schedule.isDue(februaryHeapYearDayBeforeLast, null)).isFalse()
        assertThat(schedule.isDue(januaryDayBeforeLast, null)).isFalse()
        assertThat(schedule.isDue(aprilDayBeforeLast, null)).isFalse()
    }

    @Test
    fun `MonthlySchedule is due, check WeekDayMonthRelated`() {
        val schedule: Schedule = Schedule.MonthlyScheduleByDueDatesIndices(
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
            startFromHabitStart = true,
            startDate = routineStartDate,
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

        assertThat(schedule.isDue(firstMonday1, null)).isTrue()
        assertThat(schedule.isDue(firstMonday2, null)).isTrue()
        assertThat(schedule.isDue(thirdTuesday, null)).isTrue()
        assertThat(schedule.isDue(secondFriday, null)).isTrue()
        assertThat(schedule.isDue(forthMonday, null)).isTrue()
        assertThat(schedule.isDue(lastSundayAndLastDayOfMonth, null)).isTrue()
        assertThat(schedule.isDue(forthAndLastSunday, null)).isTrue()
        assertThat(schedule.isDue(forthNotLastSunday, null)).isFalse()
        assertThat(schedule.isDue(fifthThursday, null)).isTrue()
        assertThat(schedule.isDue(forthThursday, null)).isFalse()
        assertThat(schedule.isDue(forthWednesday, null)).isTrue()
        assertThat(schedule.isDue(secondTuesday, null)).isFalse()
    }

    @Test
    fun `MonthlyScheduleByNumOfDueDays, first period is short, assert fails with an exception`() {
        assertFailsWith<IllegalStateException> {
            // should fail because routine start date is not the first day of month
            Schedule.MonthlyScheduleByNumOfDueDays(
                startDate = routineStartDate.plusDays(Random.nextInt(1, 27)),
                numOfDueDays = 18,
                startFromHabitStart = false,
                numOfDueDaysInFirstPeriod = null,
            )
        }
    }

    @Test
    fun `MonthlyScheduleByNumOfDueDays, start from routine start, due on specified days`() {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            startDate = routineStartDate,
            numOfDueDays = Random.nextInt(2, 30),
            startFromHabitStart = true,
            numOfDueDaysInFirstPeriod = null,
        )

        for (dueDayMonthNumber in 1..schedule.numOfDueDays) {
            val dueDate = LocalDate(2023, Random.nextInt(1, 12), dueDayMonthNumber)
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isTrue()
        }

        for (notDueDayNumber in (schedule.numOfDueDays + 1)..28) {
            val dueDate = LocalDate(2023, Random.nextInt(1, 12), notDueDayNumber)
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isFalse()
        }
    }

    @Test
    fun `MonthlyScheduleByNumOfDueDays, assert num of due days more than 28 doesn't introduce backlog`() {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            startDate = routineStartDate,
            startFromHabitStart = true,
            numOfDueDaysInFirstPeriod = null,
            numOfDueDays = 29,
        )

        for (dayIndex in 0..28) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isTrue()
        }

        for (dayIndex in 29..30) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isFalse()
        }

        for (dayIndex in 31..87) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isTrue()
        }

        for (dayIndex in 88..89) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isFalse()
        }
    }

    @Test
    fun periodicCustomScheduleIsDue() {
        val numOfDaysInPeriod = Random.nextInt(2, 100)
        val dueDaysNumber = numOfDaysInPeriod / 2

        val schedule: Schedule = Schedule.AlternateDaysSchedule(
            numOfDueDays = dueDaysNumber,
            numOfDaysInPeriod = numOfDaysInPeriod,
            startDate = routineStartDate,
        )

        for (dueDayNumber in 1..dueDaysNumber) {
            val numOfPeriodsAlreadyPassed = Random.nextInt(1, 50)
            assertThat(
                schedule.isDue(
                    validationDate = routineStartDate.plus(
                        DatePeriod(
                            // subtract one because indices start count from 1, not from 0
                            days = numOfDaysInPeriod * numOfPeriodsAlreadyPassed + dueDayNumber - 1
                        )
                    )
                )
            ).isTrue()
        }

        for (notDueDayIndex in (dueDaysNumber + 1)..numOfDaysInPeriod) {
            val numOfPeriodsAlreadyPassed = Random.nextInt(1, 50)
            assertThat(
                schedule.isDue(
                    validationDate = routineStartDate.plus(
                        DatePeriod(
                            // subtract one because indices start count from 1, not from 0
                            days = numOfDaysInPeriod * numOfPeriodsAlreadyPassed + notDueDayIndex - 1
                        )
                    )
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
            startDate = routineStartDate,
            dueDates = dueDates,
        )

        for (dueDate in dueDates) {
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isTrue()
        }

        for (notDueDate in notDueDates) {
            assertThat(
                schedule.isDue(validationDate = notDueDate)
            ).isFalse()
        }
    }

    @Test
    fun annualScheduleByDueDaysOfYearIsDue() {
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
                    year = Random.nextInt(2024, 2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth
                )
            )
            expectedDueDates.add(
                LocalDate(
                    year = Random.nextInt(2024, 2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth,
                )
            )
        }

        val expectedNotDueDates = mutableListOf<LocalDate>()

        notDueDates.forEach {
            expectedNotDueDates.add(
                LocalDate(
                    year = Random.nextInt(2024, 2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth
                )
            )
            expectedNotDueDates.add(
                LocalDate(
                    year = Random.nextInt(2024, 2200),
                    month = it.month,
                    dayOfMonth = it.dayOfMonth,
                )
            )
        }

        dueDates.add(AnnualDate(Month.FEBRUARY, 29))
        expectedDueDates.add(LocalDate(2024, Month.FEBRUARY, 29))
        expectedNotDueDates.add(LocalDate(2025, Month.FEBRUARY, 28))

        val schedule = Schedule.AnnualScheduleByDueDates(
            dueDates = dueDates,
            startDate = routineStartDate,
            startFromHabitStart = false,
        )

        for (dueDate in expectedDueDates) {
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isTrue()
        }

        for (notDueDate in expectedNotDueDates) {
            assertThat(
                schedule.isDue(validationDate = notDueDate)
            ).isFalse()
        }
    }

    @Test
    fun `AnnualScheduleByNumOfDueDays, first period is short, fails with an exception`() {
        assertFailsWith<IllegalStateException> {
            Schedule.AnnualScheduleByNumOfDueDays(
                startDate = routineStartDate.plusDays(Random.nextInt(1, 365)),
                startFromHabitStart = false,
                numOfDueDays = 0,
                numOfDueDaysInFirstPeriod = null,
            )
        }
    }

    @Test
    fun `AnnualScheduleByNumOfDueDays, start from routine start, due on specified days`() {

        val schedule = Schedule.AnnualScheduleByNumOfDueDays(
            startDate = routineStartDate,
            numOfDueDays = Random.nextInt(2, 364),
            startFromHabitStart = true,
            numOfDueDaysInFirstPeriod = null,
        )

        for (dueDayNumber in 1..schedule.numOfDueDays) {
            val dueDate = LocalDate(Random.nextInt(2023, 2030), 1, 1)
                .plusDays(dueDayNumber - 1)
            assertThat(
                schedule.isDue(validationDate = dueDate)
            ).isTrue()
        }

        for (notDueDayNumber in (schedule.numOfDueDays + 1)..364) {
            val notDueDate = LocalDate(Random.nextInt(2023, 2030), 1, 1)
                .plusDays(notDueDayNumber - 1)
            assertThat(
                schedule.isDue(validationDate = notDueDate)
            ).isFalse()
        }
    }
}
