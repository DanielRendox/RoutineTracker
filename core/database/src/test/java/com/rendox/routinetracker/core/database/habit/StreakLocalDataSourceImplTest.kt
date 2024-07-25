package com.rendox.routinetracker.core.database.habit

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSourceImpl
import com.rendox.routinetracker.core.model.Streak
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

class StreakLocalDataSourceImplTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver
    private lateinit var streakLocalDataSource: StreakLocalDataSource
    private val habitId = 1L

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
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)

        streakLocalDataSource = StreakLocalDataSourceImpl(
            db = get(),
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `getLongestStreak returns correct streak`() = runTest {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 1),
                endDate = LocalDate(2024, 7, 2),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 4),
                endDate = LocalDate(2024, 8, 1),
            ),
        )
        streakLocalDataSource.insertStreaks(mapOf(habitId to streaks))
        assertThat(streakLocalDataSource.getLongestStreaks(habitId)).containsExactly(streaks.last())
    }

    @Test
    fun `getLongestStreak joins adjacent streaks`() = runTest {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 1),
                endDate = LocalDate(2024, 7, 2),
            ),
            Streak(
                startDate = LocalDate(2024, 8, 1),
                endDate = LocalDate(2024, 8, 6),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 3),
                endDate = LocalDate(2024, 7, 7),
            ),
        )
        streakLocalDataSource.insertStreaks(mapOf(habitId to streaks))
        val expected = Streak(
            startDate = LocalDate(2024, 7, 1),
            endDate = LocalDate(2024, 7, 7),
        )
        assertThat(streakLocalDataSource.getLongestStreaks(habitId)).containsExactly(expected)
    }

    @Test
    fun `getLongestStreak handles one-day streaks`() = runTest {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 1),
                endDate = LocalDate(2024, 7, 1),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 2),
                endDate = LocalDate(2024, 7, 2),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 4),
                endDate = LocalDate(2024, 7, 4),
            ),
        )
        streakLocalDataSource.insertStreaks(mapOf(habitId to streaks))
        val expected = Streak(
            startDate = LocalDate(2024, 7, 1),
            endDate = LocalDate(2024, 7, 2),
        )
        assertThat(streakLocalDataSource.getLongestStreaks(habitId)).containsExactly(expected)
    }

    @Test
    fun `getLongestStreak returns all streaks with the same duration`() = runTest {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 1),
                endDate = LocalDate(2024, 7, 7),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 9),
                endDate = LocalDate(2024, 7, 15),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 17),
                endDate = LocalDate(2024, 7, 18),
            ),
        )
        streakLocalDataSource.insertStreaks(mapOf(habitId to streaks))
        val longestStreaks = streakLocalDataSource.getLongestStreaks(habitId)
        assertThat(longestStreaks).containsExactlyElementsIn(streaks.dropLast(1))
    }

    @Test
    fun `getLongestStreak returns empty list when there are no streaks`() = runTest {
        assertThat(streakLocalDataSource.getLongestStreaks(habitId)).isEmpty()
    }

    @Test
    fun `getLastStreak returns correct streak`() = runTest {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 1),
                endDate = LocalDate(2024, 7, 2),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 5),
                endDate = LocalDate(2024, 7, 5),
            ),
        )
        streakLocalDataSource.insertStreaks(mapOf(habitId to streaks))
        assertThat(streakLocalDataSource.getLastStreak(habitId)).isEqualTo(streaks.last())
    }

    @Test
    fun `getLastStreak joins adjacent steaks`() = runTest {
        val streaks = listOf(
            Streak(
                startDate = LocalDate(2024, 7, 10),
                endDate = LocalDate(2024, 7, 12),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 13),
                endDate = LocalDate(2024, 7, 15),
            ),
            Streak(
                startDate = LocalDate(2024, 7, 16),
                endDate = LocalDate(2024, 7, 16),
            ),
        )
        streakLocalDataSource.insertStreaks(mapOf(habitId to streaks))
        val expected = Streak(
            startDate = streaks.first().startDate,
            endDate = streaks.last().endDate,
        )
        assertThat(streakLocalDataSource.getLastStreak(habitId)).isEqualTo(expected)
    }

    @Test
    fun `getLastStreak returns null when there are no streaks`() = runTest {
        assertThat(streakLocalDataSource.getLastStreak(habitId)).isNull()
    }
}