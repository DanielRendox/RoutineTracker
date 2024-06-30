package com.rendox.routinetracker.core.database.habit

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSourceImpl
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class CompletionHistoryLocalDataSourceImplTest : KoinTest {
    private lateinit var sqlDriver: SqlDriver
    private lateinit var completionHistoryLocalDataSource: CompletionHistoryLocalDataSource

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    private val habit = Habit.YesNoHabit(
        id = 1L,
        name = "Test habit",
        schedule = Schedule.EveryDaySchedule(
            startDate = LocalDate(2024, 1, 1),
        ),
    )
    private val testCompletionHistory = listOf(
        Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 1, 1),
            numOfTimesCompleted = 1F,
        ),
        Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 3, 2),
            numOfTimesCompleted = 1F,
        ),
        Habit.YesNoHabit.CompletionRecord(
            date = LocalDate(2024, 5, 3),
            numOfTimesCompleted = 1F,
        ),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() = runTest {
        startKoin {
            modules(
                localDataSourceModule,
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)

        completionHistoryLocalDataSource = CompletionHistoryLocalDataSourceImpl(
            db = get(),
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `assert getRecordsInPeriod returns only records within the given period`() = runTest {
        completionHistoryLocalDataSource.insertCompletions(
            completions = mapOf(1L to testCompletionHistory),
        )
        val records = completionHistoryLocalDataSource.getRecordsInPeriod(
            habit = habit,
            minDate = LocalDate(2024, 2, 1),
            maxDate = LocalDate(2024, 8, 1),
        )
        assertThat(records).containsExactly(
            testCompletionHistory[1],
            testCompletionHistory[2],
        )
    }

    @Test
    fun `assert getMultiHabitRecords returns only records within the given period`() = runTest {
        completionHistoryLocalDataSource.insertCompletions(
            completions = mapOf(
                1L to testCompletionHistory,
                2L to testCompletionHistory,
                3L to testCompletionHistory,
            ),
        )
        val habitsToPeriods = listOf(
            listOf(habit, habit.copy(id = 2)) to LocalDate(2024, 2, 1)..LocalDate(2024, 8, 1),
            listOf(habit.copy(id = 3)) to LocalDate(2024, 5, 3)..LocalDate(2024, 5, 3),
            listOf(habit.copy(id = 4)) to LocalDate(2024, 1, 1)..LocalDate(2024, 1, 1),
        )
        val records = completionHistoryLocalDataSource.getMultiHabitRecords(habitsToPeriods)
        val expectedRecords = mapOf(
            1L to testCompletionHistory.takeLast(2),
            2L to testCompletionHistory.takeLast(2),
            3L to testCompletionHistory.takeLast(1),
        )
        assertThat(records).containsExactlyEntriesIn(expectedRecords)
    }
}