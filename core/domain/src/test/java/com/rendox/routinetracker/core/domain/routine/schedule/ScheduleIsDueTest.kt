package com.rendox.routinetracker.core.domain.routine.schedule

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.domain.completion_history.isDue
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
            routineStartDate = routineStartDate,
        )
        val date1 = LocalDate(2023, (1..12).random(), (1..28).random())
        val date2 = LocalDate(2024, Month.FEBRUARY, 29)
        assertThat(
            schedule.isDue(
                validationDate = date1,
                actualDate = null,
                numOfTimesCompletedInCurrentPeriod = 0.0,
            )
        ).isTrue()
        assertThat(
            schedule.isDue(
                validationDate = date2,
                actualDate = null,
                numOfTimesCompletedInCurrentPeriod = 0.0,
            )
        ).isTrue()
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
            routineStartDate = routineStartDate,
            dueDaysOfWeek = dueDaysOfWeek1,
        )
        val schedule2: Schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
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

        assertThat(schedule1.isDue(monday, null, 0.0)).isTrue()
        assertThat(schedule1.isDue(wednesday, null, 0.0)).isTrue()
        assertThat(schedule1.isDue(thursday, null, 0.0)).isTrue()
        assertThat(schedule1.isDue(sunday, null, 0.0)).isTrue()
        assertThat(schedule1.isDue(tuesday, null, 0.0)).isFalse()
        assertThat(schedule1.isDue(friday, null, 0.0)).isFalse()
        assertThat(schedule1.isDue(saturday, null, 0.0)).isFalse()

        assertThat(schedule2.isDue(tuesday, null, 0.0)).isTrue()
        assertThat(schedule2.isDue(wednesday, null, 0.0)).isTrue()
        assertThat(schedule2.isDue(friday, null, 0.0)).isTrue()
        assertThat(schedule2.isDue(saturday, null, 0.0)).isTrue()
        assertThat(schedule2.isDue(monday, null, 0.0)).isFalse()
        assertThat(schedule2.isDue(thursday, null, 0.0)).isFalse()
        assertThat(schedule2.isDue(sunday, null, 0.0)).isFalse()
    }

    @Test
    fun `weekly schedule, first period is short, assert fails with an exception`() {
        assertFailsWith<IllegalStateException> {
            Schedule.WeeklyScheduleByNumOfDueDays(
                routineStartDate = routineStartDate,
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
            routineStartDate = routineStartDate,
            numOfDueDays = numOfDueDays,
            startDayOfWeek = null,
            numOfDueDaysInFirstPeriod = null,
        )

        val futurePeriodStart = schedule.routineStartDate.plusDays(DateTimeUnit.WEEK.days)
        val dateScheduleDeviationIsActualFor =
            schedule.routineStartDate.plusDays(DateTimeUnit.WEEK.days - 1)
        for (dayIndex in 0 until numOfDueDays) {
            assertThat(
                schedule.isDue(
                    validationDate = futurePeriodStart.plusDays(dayIndex),
                    actualDate = dateScheduleDeviationIsActualFor,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (dayIndex in numOfDueDays until DateTimeUnit.WEEK.days) {
            assertThat(
                schedule.isDue(
                    validationDate = futurePeriodStart.plusDays(dayIndex),
                    actualDate = dateScheduleDeviationIsActualFor,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }
    }

    @Test
    fun `weekly schedule due X days per week, assert skipped, then due X days, then not due`() {
        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            routineStartDate = routineStartDate,
            numOfDueDays = 3,
            startDayOfWeek = null,
            numOfDueDaysInFirstPeriod = null,
        )

        for (dayIndex in 0..2) {
            val currentDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (dayIndex in 3..5) {
            val currentDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = routineStartDate.plusDays(2),
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        assertThat(
            schedule.isDue(
                validationDate = routineStartDate.plusDays(6),
                actualDate = routineStartDate.plusDays(2),
                numOfTimesCompletedInCurrentPeriod = 0.0,
            )
        ).isFalse()
    }

    @Test
    fun `weekly schedule due X days per week, assert not due because all completed`() {
        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            routineStartDate = routineStartDate,
            numOfDueDays = 4,
            startDayOfWeek = null,
            numOfDueDaysInFirstPeriod = null,
        )

        for (dayIndex in 4..6) {
            val currentDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = routineStartDate.plusDays(3),
                    numOfTimesCompletedInCurrentPeriod = 4.0,
                )
            ).isFalse()
        }
    }

    @Test
    fun `weekly schedule due X days per week, some days skipped, assert not due because all completed`() {
        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            routineStartDate = routineStartDate,
            numOfDueDays = 4,
            startDayOfWeek = null,
            numOfDueDaysInFirstPeriod = null,
        )

        assertThat(
            schedule.isDue(
                validationDate = routineStartDate.plusDays(6),
                actualDate = routineStartDate.plusDays(5),
                numOfTimesCompletedInCurrentPeriod = 4.0,
            )
        ).isFalse()
    }

    @Test
    fun `weekly schedule due X days per week, assert works fine when first period is short`() {
        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            routineStartDate = LocalDate(2023, Month.NOVEMBER, 1), // wednesday
            numOfDueDays = 4,
            startDayOfWeek = DayOfWeek.MONDAY,
            numOfDueDaysInFirstPeriod = 3,
        )

        for (dayIndex in 0..2) {
            val currentDate = schedule.routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = routineStartDate,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (dayIndex in 3..4) {
            val currentDate = schedule.routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = routineStartDate,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }

        for (dayIndex in 5..8) {
            val currentDate = schedule.routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = routineStartDate,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (dayIndex in 9..11) {
            val currentDate = schedule.routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = routineStartDate,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }

        for (dayIndex in 12..15) {
            val currentDate = schedule.routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = schedule.routineStartDate.plusDays(11),
                    numOfTimesCompletedInCurrentPeriod = 4.0,
                )
            ).isTrue()
        }

        for (dayIndex in 16..18) {
            val currentDate = schedule.routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = currentDate,
                    actualDate = schedule.routineStartDate.plusDays(15),
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
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
            startFromRoutineStart = true,
            weekDaysMonthRelated = emptyList(),
            routineStartDate = routineStartDate,
        )

        for (dueDate in dueDates) {
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (notDueDate in notDueDates) {
            assertThat(
                schedule.isDue(
                    validationDate = notDueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
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
            startFromRoutineStart = true,
            weekDaysMonthRelated = emptyList(),
            routineStartDate = routineStartDate,
        )

        assertThat(schedule.isDue(februaryHeapYearLastDate, null, 0.0)).isTrue()
        assertThat(schedule.isDue(februaryLastDate, null, 0.0)).isTrue()
        assertThat(schedule.isDue(januaryLastDate, null, 0.0)).isTrue()
        assertThat(schedule.isDue(aprilLastDate, null, 0.0)).isTrue()
        assertThat(schedule.isDue(februaryHeapYearDayBeforeLast, null, 0.0)).isFalse()
        assertThat(schedule.isDue(januaryDayBeforeLast, null, 0.0)).isFalse()
        assertThat(schedule.isDue(aprilDayBeforeLast, null, 0.0)).isFalse()
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

        assertThat(schedule.isDue(firstMonday1, null, 0.0)).isTrue()
        assertThat(schedule.isDue(firstMonday2, null, 0.0)).isTrue()
        assertThat(schedule.isDue(thirdTuesday, null, 0.0)).isTrue()
        assertThat(schedule.isDue(secondFriday, null, 0.0)).isTrue()
        assertThat(schedule.isDue(forthMonday, null, 0.0)).isTrue()
        assertThat(schedule.isDue(lastSundayAndLastDayOfMonth, null, 0.0)).isTrue()
        assertThat(schedule.isDue(forthAndLastSunday, null, 0.0)).isTrue()
        assertThat(schedule.isDue(forthNotLastSunday, null, 0.0)).isFalse()
        assertThat(schedule.isDue(fifthThursday, null, 0.0)).isTrue()
        assertThat(schedule.isDue(forthThursday, null, 0.0)).isFalse()
        assertThat(schedule.isDue(forthWednesday, null, 0.0)).isTrue()
        assertThat(schedule.isDue(secondTuesday, null, 0.0)).isFalse()
    }

    @Test
    fun `MonthlyScheduleByNumOfDueDays, first period is short, assert fails with an exception`() {
        assertFailsWith<IllegalStateException> {
            // should fail because routine start date is not the first day of month
            Schedule.MonthlyScheduleByNumOfDueDays(
                routineStartDate = routineStartDate.plusDays(Random.nextInt(1, 27)),
                numOfDueDays = 18,
                startFromRoutineStart = false,
                numOfDueDaysInFirstPeriod = null,
            )
        }
    }

    @Test
    fun `MonthlyScheduleByNumOfDueDays, start from routine start, due on specified days`() {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            routineStartDate = routineStartDate,
            numOfDueDays = Random.nextInt(2, 30),
            startFromRoutineStart = true,
            numOfDueDaysInFirstPeriod = null,
        )

        for (dueDayMonthNumber in 1..schedule.numOfDueDays) {
            val dueDate = LocalDate(2023, Random.nextInt(1, 12), dueDayMonthNumber)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (notDueDayNumber in (schedule.numOfDueDays + 1)..28) {
            val dueDate = LocalDate(2023, Random.nextInt(1, 12), notDueDayNumber)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }
    }

    @Test
    fun `MonthlyScheduleByNumOfDueDays, backlog is resolvable at inconsistent days`() {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            routineStartDate = routineStartDate,
            numOfDueDays = 10,
            startFromRoutineStart = true,
            numOfDueDaysInFirstPeriod = null,
        )

        for (dayIndex in 28..30) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = LocalDate(2023, Month.JANUARY, 28),
                    numOfTimesCompletedInCurrentPeriod = 5.0,
                )
            ).isTrue()
        }

        for (dayIndex in 31..40) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = LocalDate(2023, Month.JANUARY, 28),
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (dayIndex in 41..58) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = LocalDate(2023, Month.JANUARY, 28),
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }
    }

    @Test
    fun `MonthlyScheduleByNumOfDueDays, assert num of due days more than 28 doesn't introduce backlog`() {
        val schedule = Schedule.MonthlyScheduleByNumOfDueDays(
            routineStartDate = routineStartDate,
            startFromRoutineStart = true,
            numOfDueDaysInFirstPeriod = null,
            numOfDueDays = 29,
        )

        for (dayIndex in 0..28) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (dayIndex in 29..30) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }

        for (dayIndex in 31..87) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (dayIndex in 88..89) {
            val dueDate = routineStartDate.plusDays(dayIndex)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }
    }

    @Test
    fun periodicCustomScheduleIsDue() {
        val numOfDaysInPeriod = Random.nextInt(2, 100)
        val dueDaysNumber = numOfDaysInPeriod / 2

        val schedule: Schedule = Schedule.PeriodicCustomSchedule(
            numOfDueDays = dueDaysNumber,
            numOfDaysInPeriod = numOfDaysInPeriod,
            routineStartDate = routineStartDate,
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
                    ),
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                    scheduleDeviationInCurrentPeriod = 0.0,
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
                    ),
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                    scheduleDeviationInCurrentPeriod = 0.0,
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
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (notDueDate in notDueDates) {
            assertThat(
                schedule.isDue(
                    validationDate = notDueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
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
            routineStartDate = routineStartDate,
            startFromRoutineStart = false,
        )

        for (dueDate in expectedDueDates) {
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (notDueDate in expectedNotDueDates) {
            assertThat(
                schedule.isDue(
                    validationDate = notDueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }
    }

    @Test
    fun `AnnualScheduleByNumOfDueDays, first period is short, fails with an exception`() {
        assertFailsWith<IllegalStateException> {
            Schedule.AnnualScheduleByNumOfDueDays(
                routineStartDate = routineStartDate.plusDays(Random.nextInt(1, 365)),
                startFromRoutineStart = false,
                numOfDueDays = 0,
                numOfDueDaysInFirstPeriod = null,
            )
        }
    }

    @Test
    fun `AnnualScheduleByNumOfDueDays, start from routine start, due on specified days`() {

        val schedule = Schedule.AnnualScheduleByNumOfDueDays(
            routineStartDate = routineStartDate,
            numOfDueDays = Random.nextInt(2, 364),
            startFromRoutineStart = true,
            numOfDueDaysInFirstPeriod = null,
        )

        for (dueDayNumber in 1..schedule.numOfDueDays) {
            val dueDate = LocalDate(Random.nextInt(2023, 2030), 1, 1)
                .plusDays(dueDayNumber - 1)
            assertThat(
                schedule.isDue(
                    validationDate = dueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isTrue()
        }

        for (notDueDayNumber in (schedule.numOfDueDays + 1)..364) {
            val notDueDate = LocalDate(Random.nextInt(2023, 2030), 1, 1)
                .plusDays(notDueDayNumber - 1)
            assertThat(
                schedule.isDue(
                    validationDate = notDueDate,
                    actualDate = null,
                    numOfTimesCompletedInCurrentPeriod = 0.0,
                )
            ).isFalse()
        }
    }
}
