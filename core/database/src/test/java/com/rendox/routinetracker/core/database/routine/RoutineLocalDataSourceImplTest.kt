package com.rendox.routinetracker.core.database.routine

import com.google.common.truth.Truth.assertThat
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.dataModule
import com.rendox.routinetracker.core.logic.time.AnnualDate
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.logic.time.WeekDayNumberMonthRelated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineLocalDataSourceImplTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                dataModule,
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun getInsertYesNoRoutine() = runTest {
        val routineLocalDataSource = RoutineLocalDataSourceImpl(
            db = get(), dispatcher = UnconfinedTestDispatcher(testScheduler)
        )
        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Programming",
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
            backlogEnabled = true,
            vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
            vacationEndDate = null,
            schedule = Schedule.EveryDaySchedule,
        )

        routineLocalDataSource.insertRoutine(routine)
        val resultingRoutine = routineLocalDataSource.getRoutineById(routine.id!!)
        assertThat(resultingRoutine).isEqualTo(routine)
    }

    @Test
    fun getInsertRoutineWithWeeklySchedule() = runTest {
        val routineLocalDataSource = RoutineLocalDataSourceImpl(
            db = get(), dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val dueDaysOfWeek = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.SUNDAY,
            DayOfWeek.SATURDAY,
        )

        val schedule = Schedule.WeeklySchedule(
            dueDaysOfWeek = dueDaysOfWeek,
            startDayOfWeek = DayOfWeek.WEDNESDAY,
        )

        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Programming",
            startDate = LocalDate(2023, Month.SEPTEMBER, 28),
            backlogEnabled = true,
            vacationStartDate = LocalDate(2023, Month.OCTOBER, 10),
            vacationEndDate = null,
            schedule = schedule,
        )

        routineLocalDataSource.insertRoutine(routine)
        val resultingRoutine = routineLocalDataSource.getRoutineById(routine.id!!)
        assertThat(resultingRoutine).isEqualTo(routine)
    }

    @Test
    fun getInsertRoutineWithPeriodicCustomSchedule() = runTest {
        val routineLocalDataSource = RoutineLocalDataSourceImpl(
            db = get(), dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val dueDatesIndices = listOf(1, 3, 4, 5, 18, 31, 30, 21, 8)
        val weekDaysMonthRelated = listOf(
            WeekDayMonthRelated(DayOfWeek.MONDAY, WeekDayNumberMonthRelated.First),
            WeekDayMonthRelated(DayOfWeek.WEDNESDAY, WeekDayNumberMonthRelated.First),
            WeekDayMonthRelated(DayOfWeek.THURSDAY, WeekDayNumberMonthRelated.Third),
            WeekDayMonthRelated(DayOfWeek.FRIDAY, WeekDayNumberMonthRelated.Last)
        )

        val schedule = Schedule.MonthlySchedule(
            dueDatesIndices = dueDatesIndices,
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = weekDaysMonthRelated,
            startFromRoutineStart = true,
        )

        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Studying",
            startDate = LocalDate(2023, Month.SEPTEMBER, 28),
            backlogEnabled = false,
            vacationStartDate = LocalDate(2023, Month.OCTOBER, 10),
            vacationEndDate = LocalDate(2023, Month.NOVEMBER, 1),
            schedule = schedule,
        )

        routineLocalDataSource.insertRoutine(routine)
        val resultingRoutine = routineLocalDataSource.getRoutineById(routine.id!!)
        assertThat(resultingRoutine).isEqualTo(routine)
    }

    @Test
    fun getInsertRoutineWithMonthlyScheduleOnlyLastDayOfMonth() = runTest {
        val routineLocalDataSource = RoutineLocalDataSourceImpl(
            db = get(), dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val schedule = Schedule.MonthlySchedule(
            dueDatesIndices = emptyList(),
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = emptyList(),
            startFromRoutineStart = true,
        )

        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Studying",
            startDate = LocalDate(2023, Month.SEPTEMBER, 28),
            backlogEnabled = false,
            vacationStartDate = LocalDate(2023, Month.OCTOBER, 10),
            vacationEndDate = LocalDate(2023, Month.NOVEMBER, 1),
            schedule = schedule,
        )

        routineLocalDataSource.insertRoutine(routine)
        val resultingRoutine = routineLocalDataSource.getRoutineById(routine.id!!)
        assertThat(resultingRoutine).isEqualTo(routine)
    }

    @Test
    fun getInsertRoutineWithCustomDateSchedule() = runTest {
        val routineLocalDataSource = RoutineLocalDataSourceImpl(
            db = get(), dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val dueDates = listOf(
            LocalDate(2023, Month.OCTOBER, 4),
            LocalDate(2023, Month.OCTOBER, 15),
            LocalDate(2023, Month.JULY, 30),
            LocalDate(2024, Month.JANUARY, 1),
        )

        val schedule = Schedule.CustomDateSchedule(
            dueDates = dueDates,
        )

        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Studying",
            startDate = LocalDate(2023, Month.SEPTEMBER, 28),
            backlogEnabled = false,
            vacationStartDate = LocalDate(2023, Month.OCTOBER, 10),
            vacationEndDate = LocalDate(2023, Month.NOVEMBER, 1),
            schedule = schedule,
        )

        routineLocalDataSource.insertRoutine(routine)
        val resultingRoutine = routineLocalDataSource.getRoutineById(routine.id!!)
        assertThat(resultingRoutine).isEqualTo(routine)
    }

    @Test
    fun getInsertRoutineWithAnnualSchedule() = runTest {
        val routineLocalDataSource = RoutineLocalDataSourceImpl(
            db = get(), dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val dueDates = listOf(
            AnnualDate(Month.JANUARY, 1),
            AnnualDate(Month.FEBRUARY, 29),
            AnnualDate(Month.MAY, 25),
            AnnualDate(Month.SEPTEMBER, 30),
        )

        val schedule = Schedule.AnnualSchedule(
            dueDates = dueDates,
            startDayOfYear = AnnualDate(Month.MAY, 25)
        )

        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Studying",
            startDate = LocalDate(2023, Month.SEPTEMBER, 28),
            backlogEnabled = false,
            vacationStartDate = LocalDate(2023, Month.OCTOBER, 10),
            vacationEndDate = LocalDate(2023, Month.NOVEMBER, 1),
            schedule = schedule,
        )

        routineLocalDataSource.insertRoutine(routine)
        val resultingRoutine = routineLocalDataSource.getRoutineById(routine.id!!)
        assertThat(resultingRoutine).isEqualTo(routine)
    }
}