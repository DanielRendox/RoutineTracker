package com.rendox.routinetracker.core.database.habit

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.habitLocalDataModule
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.model.AnnualDate
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.WeekDayMonthRelated
import com.rendox.routinetracker.core.model.WeekDayNumberMonthRelated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class HabitLocalDataSourceImplTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver
    private lateinit var db: RoutineTrackerDatabase
    private lateinit var habitLocalDataSource: HabitLocalDataSource

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        startKoin {
            modules(
                localDataSourceModule,
                habitLocalDataModule,
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)
        sqlDriver.execute(null, "PRAGMA foreign_keys = ON;", 0)

        db = get()
        habitLocalDataSource = HabitLocalDataSourceImpl(
            db = db,
            ioDispatcher = UnconfinedTestDispatcher(),
            scheduleLocalDataSource = get(),
        )
    }

    @AfterEach
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
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        )

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = dueDaysOfWeek,
            startDayOfWeek = DayOfWeek.WEDNESDAY,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
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
    fun getInsertRoutineWithAlternateDaysSchedule() = runTest {
        val dueDatesIndices = listOf(1, 3, 4, 5, 8, 18, 21, 30, 31)
        val weekDaysMonthRelated = listOf(
            WeekDayMonthRelated(DayOfWeek.MONDAY, WeekDayNumberMonthRelated.First),
            WeekDayMonthRelated(DayOfWeek.WEDNESDAY, WeekDayNumberMonthRelated.First),
            WeekDayMonthRelated(DayOfWeek.THURSDAY, WeekDayNumberMonthRelated.Third),
            WeekDayMonthRelated(DayOfWeek.FRIDAY, WeekDayNumberMonthRelated.Last),
        )

        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = dueDatesIndices,
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = weekDaysMonthRelated,
            startFromHabitStart = true,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
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
                ),
            ),
            startFromHabitStart = true,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
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
            LocalDate(2023, Month.JULY, 30),
            LocalDate(2023, Month.OCTOBER, 4),
            LocalDate(2023, Month.OCTOBER, 15),
            LocalDate(2024, Month.JANUARY, 1),
        )

        val schedule = Schedule.CustomDateSchedule(
            dueDates = dueDates,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
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
            AnnualDate(Month.MAY, 25),
            AnnualDate(Month.SEPTEMBER, 30),
            AnnualDate(Month.FEBRUARY, 29),
        )

        val schedule = Schedule.AnnualScheduleByDueDates(
            dueDates = dueDates,
            startDate = LocalDate(2023, Month.SEPTEMBER, 1),
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

    @Test
    fun `deleting habit removes habit, its schedule, and due dates`() = runTest {
        val habitId = 1L
        val habit = Habit.YesNoHabit(
            id = habitId,
            name = "Test Habit",
            schedule = Schedule.MonthlyScheduleByDueDatesIndices(
                startDate = LocalDate(2024, 1, 1),
                dueDatesIndices = listOf(1, 2, 3),
                weekDaysMonthRelated = listOf(
                    WeekDayMonthRelated(DayOfWeek.MONDAY, WeekDayNumberMonthRelated.First),
                    WeekDayMonthRelated(DayOfWeek.TUESDAY, WeekDayNumberMonthRelated.Second),
                ),
            ),
        )
        habitLocalDataSource.insertHabit(habit)
        val completionTimeDate = LocalDate(2024, 1, 1)
        db.specificDateCustomCompletionTimeQueries.insertCompletiontime(
            routineId = habitId,
            date = completionTimeDate,
            completionTimeHour = 12,
            completionTimeMinute = 30,
        )
        db.vacationEntityQueries.insertVacation(
            habitId = habitId,
            startDate = LocalDate(2024, 1, 4),
            endDate = LocalDate(2024, 1, 4),
        )

        habitLocalDataSource.deleteHabitById(habitId)

        assertThat(db.habitEntityQueries.getHabitById(habitId).executeAsOneOrNull()).isNull()
        assertThat(db.scheduleEntityQueries.getScheduleById(habitId).executeAsOneOrNull()).isNull()
        assertThat(db.dueDateEntityQueries.getDueDates(habitId).executeAsList()).isEmpty()
        assertThat(db.weekDayMonthRelatedEntityQueries.getWeekDayMonthRelatedDays(habitId).executeAsList()).isEmpty()
    }

    @Test
    fun `deleting habit removes all its completions`() = runTest {
        val habitId = 1L
        val habit = Habit.YesNoHabit(
            id = habitId,
            name = "Test Habit",
            schedule = Schedule.EveryDaySchedule(
                startDate = LocalDate(2024, 1, 1),
            ),
        )
        habitLocalDataSource.insertHabit(habit)
        db.cachedStreakEntityQueries.insertStreak(
            habitId = habitId,
            startDate = LocalDate(2024, 1, 1),
            endDate = LocalDate(2024, 1, 3),
        )
        habitLocalDataSource.deleteHabitById(habitId)
        assertThat(db.cachedStreakEntityQueries.getAllStreaks(habitId).executeAsList()).isEmpty()
    }

    @Test
    fun `deleting habit removes all its streaks`() = runTest {
        val habitId = 1L
        val habit = Habit.YesNoHabit(
            id = habitId,
            name = "Test Habit",
            schedule = Schedule.EveryDaySchedule(
                startDate = LocalDate(2024, 1, 1),
            ),
        )
        habitLocalDataSource.insertHabit(habit)
        assertThat(db.cachedStreakEntityQueries.getAllStreaks(habitId).executeAsList()).isEmpty()
    }

    @Test
    fun `deleting habit removes all vacations`() = runTest {
        val habitId = 1L
        val habit = Habit.YesNoHabit(
            id = habitId,
            name = "Test Habit",
            schedule = Schedule.EveryDaySchedule(
                startDate = LocalDate(2024, 1, 1),
            ),
        )
        habitLocalDataSource.insertHabit(habit)
        assertThat(db.vacationEntityQueries.getAllVacations(habitId).executeAsList()).isEmpty()
    }
}