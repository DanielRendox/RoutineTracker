package com.rendox.routinetracker.core.database.routine

import com.google.common.truth.Truth.assertThat
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSourceImpl
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class StreakLocalDataSourceImplTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver
    private lateinit var streakLocalDataSource: StreakLocalDataSource
    private val routineId = 1L

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    private val initialStreaks = listOf(
        Streak(
            id = 1,
            startDate = LocalDate(2023, 1, 5),
            endDate = LocalDate(2023, 1, 7),
        ),
        Streak(
            id = 2,
            startDate = LocalDate(2023, 2, 8),
            endDate = LocalDate(2023, 2, 15),
        ),
        Streak(
            id = 3,
            startDate = LocalDate(2023, 3, 1),
            endDate = LocalDate(2023, 3, 5),
        ),
        Streak(
            id = 4,
            startDate = LocalDate(2023, 4, 1),
            endDate = LocalDate(2023, 4, 10),
        ),
        Streak(
            id = 5,
            startDate = LocalDate(2023, 5, 25),
            endDate = null,
        ),
    )

    @Before
    fun setUp() = runTest {
        startKoin {
            modules(
                localDataSourceModule,
                testModule,
            )
        }

        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RoutineTrackerDatabase.Schema.create(sqlDriver)

        streakLocalDataSource = StreakLocalDataSourceImpl(
            db = get(), dispatcher = get()
        )
        for (streak in initialStreaks) {
            streakLocalDataSource.insertStreak(streak, routineId)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun getAllStreaksWithoutParametersAssertReturnsAllStreaksTest() = runTest {
        assertThat(
            streakLocalDataSource.getAllStreaks(routineId = routineId)
        ).containsExactlyElementsIn(initialStreaks).inOrder()
    }

    @Test
    fun getAllStreaksAfterSomeDateTest() = runTest {
        assertThat(
            streakLocalDataSource.getAllStreaks(
                routineId = routineId,
                afterDateInclusive = LocalDate(2023, 3, 4),
            )
        ).containsExactlyElementsIn(initialStreaks.drop(2)).inOrder()
    }

    @Test
    fun getAllStreaksBeforeSomeDateTest() = runTest {
        assertThat(
            streakLocalDataSource.getAllStreaks(
                routineId = routineId,
                beforeDateInclusive = LocalDate(2023, 4, 6),
            )
        ).containsExactlyElementsIn(initialStreaks.take(4)).inOrder()
    }

    @Test
    fun getAllStreaksRangeBiggerThanStreaksTest() = runTest {
        assertThat(
            streakLocalDataSource.getAllStreaks(
                routineId = routineId,
                afterDateInclusive = LocalDate(2022, 1, 1),
                beforeDateInclusive = LocalDate(2024, 1, 1),
            )
        ).containsExactlyElementsIn(initialStreaks).inOrder()
    }

    @Test
    fun getFinishedStreakByDateTest() = runTest {
        assertThat(
            streakLocalDataSource.getStreakByDate(
                routineId = routineId,
                dateWithinStreak = LocalDate(2023, 4, 7),
            )
        ).isEqualTo(initialStreaks[3])
    }

    @Test
    fun getNotFinishedStreakByDateTest() = runTest {
        assertThat(
            streakLocalDataSource.getStreakByDate(
                routineId = routineId,
                dateWithinStreak = LocalDate(2023, 6, 1),
            )
        ).isEqualTo(initialStreaks.last())
    }
}