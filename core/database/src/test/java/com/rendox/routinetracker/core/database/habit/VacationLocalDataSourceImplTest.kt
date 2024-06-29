package com.rendox.routinetracker.core.database.habit

import com.google.common.truth.Truth.assertThat
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource
import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSourceImpl
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class VacationLocalDataSourceImplTest : KoinTest {
    private lateinit var sqlDriver: SqlDriver
    private lateinit var vacationLocalDataSource: VacationLocalDataSource

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    private val habitId = 1L

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

        vacationLocalDataSource = VacationLocalDataSourceImpl(
            db = get(), ioDispatcher = UnconfinedTestDispatcher()
        )
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @ParameterizedTest
    @CsvSource(
        "2024-06-03, 2024-07-02, true, true",
        "2024-06-03, 2024-07-01, true, true",
        "2024-06-08, 2024-07-02, true, true",
        "2024-06-03, 2024-06-10, true, true",
        "2024-06-10, 2024-08-01, true, true",
        "2024-06-10, 2024-06-20, true, true",
        "2024-05-01, 2024-05-02, false, true",
        "2024-08-01, 2024-08-02, false, true",
        "2025-01-01, 2025-01-02, true, false",
        "2024-05-01, 2024-05-02, false, false",
        "2024-01-01, 2025-01-01, true, false",
    )
    fun testGetVacationsInPeriod(
        minDate: String,
        maxDate: String,
        shouldContain: Boolean,
        vacationCompleted: Boolean,
    ) = runTest {
        val vacation = Vacation(
            id = 1L,
            startDate = LocalDate(2024, 6, 8),
            endDate = if (vacationCompleted) {
                LocalDate(2024, 7, 1)
            } else null,
        )
        vacationLocalDataSource.insertVacation(
            habitId = habitId,
            vacation = vacation,
        )
        val resultingVacations = vacationLocalDataSource.getVacationsInPeriod(
            habitId = habitId,
            minDate = minDate.toLocalDate(),
            maxDate = maxDate.toLocalDate(),
        )
        if (shouldContain) {
            assertThat(resultingVacations).containsExactly(vacation)
        } else {
            assertThat(resultingVacations).isEmpty()
        }
    }

    @Test
    fun testGetMultiHabitVacations() = runTest {
        val habitIdsToVacations = mapOf(
            1L to listOf(
                Vacation(
                    id = 1L,
                    startDate = LocalDate(2024, 6, 8),
                    endDate = LocalDate(2024, 7, 1),
                ),
                Vacation(
                    id = 2L,
                    startDate = LocalDate(2024, 8, 1),
                    endDate = LocalDate(2024, 8, 2),
                ),
            ),
            2L to listOf(
                Vacation(
                    id = 3L,
                    startDate = LocalDate(2024, 10, 1),
                    endDate = null,
                )
            ),
            3L to emptyList(),
            4L to listOf(
                Vacation(
                    id = 4L,
                    startDate = LocalDate(2024, 6, 3),
                    endDate = LocalDate(2024, 7, 2),
                )
            ),
        )
        vacationLocalDataSource.insertVacations(habitIdsToVacations = habitIdsToVacations)
        val vacations = vacationLocalDataSource.getMultiHabitVacations(
            habitsToPeriods = listOf(
                listOf(1L, 2L) to LocalDate(2024, 6, 1)..LocalDate(2024, 7, 1),
                listOf(3L, 4L) to LocalDate(2024, 6, 3)..LocalDate(2024, 6, 3),
            )
        )
        val expectedResult = mapOf(
            1L to habitIdsToVacations.getValue(1L).take(1),
            4L to habitIdsToVacations.getValue(4L),
        )
        assertThat(vacations).containsExactlyEntriesIn(expectedResult)
    }
}