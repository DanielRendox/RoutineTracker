package com.rendox.routinetracker.core.database.routine

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.logic.time.WeekDayNumberMonthRelated
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class HabitLocalDataSourceImplTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver
    private lateinit var habitLocalDataSource: HabitLocalDataSource

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                localDataSourceModule,
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)

        habitLocalDataSource = HabitLocalDataSourceImpl(
            db = get(), dispatcher = get()
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun getInsertYesNoRoutine() = runTest {
        val habit = Habit.YesNoHabit(
            id = 1,
            name = "Programming",
            description = "Make my app",
            sessionDurationMinutes = 120,
            progress = 0.8f,
            schedule = Schedule.EveryDaySchedule(
                startDate = LocalDate(2023, Month.SEPTEMBER, 1),
                vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
                vacationEndDate = null,
            ),
            defaultCompletionTime = LocalTime(hour = 18, minute = 30),
        )

        habitLocalDataSource.insertHabit(habit)
        val resultingRoutine = habitLocalDataSource.getHabitById(habit.id!!)
        assertThat(resultingRoutine).isEqualTo(habit)
    }

    @Test
    fun getInsertRoutineWithWeeklySchedule() = runTest {
        val dueDaysOfWeek = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.SUNDAY,
            DayOfWeek.SATURDAY,
        )

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = dueDaysOfWeek,
            startDayOfWeek = DayOfWeek.WEDNESDAY,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
            vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
            vacationEndDate = null,
            backlogEnabled = false,
            completingAheadEnabled = false,
        )

        val habit = Habit.YesNoHabit(
            id = 1,
            name = "Programming",
            schedule = schedule,
        )

        habitLocalDataSource.insertHabit(habit)
        val resultingRoutine = habitLocalDataSource.getHabitById(habit.id!!)
        assertThat(resultingRoutine).isEqualTo(habit)
    }

    @Test
    fun getInsertRoutineWithPeriodicCustomSchedule() = runTest {
        val dueDatesIndices = listOf(1, 3, 4, 5, 18, 31, 30, 21, 8)
        val weekDaysMonthRelated = listOf(
            WeekDayMonthRelated(DayOfWeek.MONDAY, WeekDayNumberMonthRelated.First),
            WeekDayMonthRelated(DayOfWeek.WEDNESDAY, WeekDayNumberMonthRelated.First),
            WeekDayMonthRelated(DayOfWeek.THURSDAY, WeekDayNumberMonthRelated.Third),
            WeekDayMonthRelated(DayOfWeek.FRIDAY, WeekDayNumberMonthRelated.Last)
        )

        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = dueDatesIndices,
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = weekDaysMonthRelated,
            startFromHabitStart = true,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
            vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
            vacationEndDate = null,
            backlogEnabled = false,
            completingAheadEnabled = false,
        )

        val habit = Habit.YesNoHabit(
            id = 1,
            name = "Studying",
            schedule = schedule,
        )

        habitLocalDataSource.insertHabit(habit)
        val resultingRoutine = habitLocalDataSource.getHabitById(habit.id!!)
        assertThat(resultingRoutine).isEqualTo(habit)
    }

    @Test
    fun getInsertRoutineWithMonthlyScheduleOnlyLastDayOfMonth() = runTest {
        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(1, 3, 5, 6, 8, 11, 25),
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = listOf(
                WeekDayMonthRelated(
                    dayOfWeek = DayOfWeek.TUESDAY,
                    weekDayNumberMonthRelated = WeekDayNumberMonthRelated.Last,
                ),
                WeekDayMonthRelated(
                    dayOfWeek = DayOfWeek.TUESDAY,
                    weekDayNumberMonthRelated = WeekDayNumberMonthRelated.Third,
                )
            ),
            startFromHabitStart = true,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
            vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
            vacationEndDate = null,
            backlogEnabled = false,
            completingAheadEnabled = false,
        )

        val habit = Habit.YesNoHabit(
            id = 1,
            name = "Studying",
            schedule = schedule,
        )

        habitLocalDataSource.insertHabit(habit)
        val resultingRoutine = habitLocalDataSource.getHabitById(habit.id!!)
        assertThat(resultingRoutine).isEqualTo(habit)
    }

    @Test
    fun getInsertRoutineWithCustomDateSchedule() = runTest {
        val dueDates = listOf(
            LocalDate(2023, Month.OCTOBER, 4),
            LocalDate(2023, Month.OCTOBER, 15),
            LocalDate(2023, Month.JULY, 30),
            LocalDate(2024, Month.JANUARY, 1),
        )

        val schedule = Schedule.CustomDateSchedule(
            dueDates = dueDates,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
            vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
            vacationEndDate = null,
            backlogEnabled = false,
            completingAheadEnabled = false,
        )

        val habit = Habit.YesNoHabit(
            id = 1,
            name = "Studying",
            schedule = schedule,
        )

        habitLocalDataSource.insertHabit(habit)
        val resultingRoutine = habitLocalDataSource.getHabitById(habit.id!!)
        assertThat(resultingRoutine).isEqualTo(habit)
    }

    @Test
    fun getInsertRoutineWithAnnualSchedule() = runTest {
        val dueDates = listOf(
            AnnualDate(Month.JANUARY, 1),
            AnnualDate(Month.FEBRUARY, 29),
            AnnualDate(Month.MAY, 25),
            AnnualDate(Month.SEPTEMBER, 30),
        )

        val schedule = Schedule.AnnualScheduleByDueDates(
            dueDates = dueDates,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
            vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
            vacationEndDate = null,
            backlogEnabled = false,
            completingAheadEnabled = false,
            startFromHabitStart = false,
        )

        val habit = Habit.YesNoHabit(
            id = 1,
            name = "Studying",
            schedule = schedule,
        )

        habitLocalDataSource.insertHabit(habit)
        val resultingRoutine = habitLocalDataSource.getHabitById(habit.id!!)
        assertThat(resultingRoutine).isEqualTo(habit)
    }
}