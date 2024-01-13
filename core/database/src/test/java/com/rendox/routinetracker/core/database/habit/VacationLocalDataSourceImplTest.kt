package com.rendox.routinetracker.core.database.habit

import com.google.common.truth.Truth.assertThat
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource
import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSourceImpl
import com.rendox.routinetracker.core.model.Vacation
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

class VacationLocalDataSourceImplTest : KoinTest {
    private lateinit var sqlDriver: SqlDriver
    private lateinit var vacationLocalDataSource: VacationLocalDataSource

    private val testModule = module {
        single {
            sqlDriver
        }
    }

    private val habitId = 1L
    private val vacationsList = listOf(
        Vacation(
            id = 1,
            startDate = LocalDate(2023, 1, 1),
            endDate = LocalDate(2023, 1, 3),
        ),  
        Vacation(
            id = 2,
            startDate = LocalDate(2023, 1, 5),
            endDate = LocalDate(2023, 1, 11),
        ),
        Vacation(
            id = 3,
            startDate = LocalDate(2023, 1, 15),
            endDate = LocalDate(2023, 1, 20),
        ),
        Vacation(
            id = 4,
            startDate = LocalDate(2023, 5, 25),
            endDate = LocalDate(2023, 5, 30),
        ),
        Vacation(
            id = 5,
            startDate = LocalDate(2023, 6, 1),
            endDate = LocalDate(2023, 6, 8),
        ),
        Vacation(
            id = 6,
            startDate = LocalDate(2023, 9, 30),
            endDate = LocalDate(2023, 9, 30),
        ),
        Vacation(
            id = 7,
            startDate = LocalDate(2023, 10, 1),
            endDate = LocalDate(2023, 11, 1),
        ),
        Vacation(
            id = 8,
            startDate = LocalDate(2023, 11, 20),
            endDate = null,
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

        vacationLocalDataSource = VacationLocalDataSourceImpl(
            db = get(), dispatcher = UnconfinedTestDispatcher()
        )
        
        for (vacation in vacationsList) {
            vacationLocalDataSource.insertVacation(
                habitId = habitId,
                vacation = vacation,
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `assert get vacations in period returns vacations within date range`() = runTest {
        val resultingVacations = vacationLocalDataSource.getVacationsInPeriod(
            habitId = habitId,
            minDate = LocalDate(2023, 1, 13),
            maxDate = LocalDate(2023, 6, 10),
        )
        val expectedVacations = vacationsList.filter { it.id in 3L..5L }
        assertThat(resultingVacations).containsExactlyElementsIn(expectedVacations)
    }

    @Test
    fun `assert get vacations in period returns vacations that contain max and min dates`() = runTest {
        val resultingVacations = vacationLocalDataSource.getVacationsInPeriod(
            habitId = habitId,
            minDate = LocalDate(2023, 1, 10),
            maxDate = LocalDate(2023, 11, 20),
        )
        val expectedVacations = vacationsList.filter { it.id!! >= 2L }
        assertThat(resultingVacations).containsExactlyElementsIn(expectedVacations)
    }

    @Test
    fun `assert get vacation by date returns vacation that contains date`() = runTest {
        val resultingVacation = vacationLocalDataSource.getVacationByDate(
            habitId = habitId,
            date = LocalDate(2023, 1, 10),
        )
        val expectedVacation = vacationsList.find { it.id == 2L }
        assertThat(resultingVacation).isEqualTo(expectedVacation)
    }

    @Test
    fun `assert previous vacation returns vacation that ends before current date`() = runTest {
        val resultingVacation = vacationLocalDataSource.getPreviousVacation(
            habitId = habitId,
            currentDate = LocalDate(2023, 9, 30),
        )
        val expectedVacation = vacationsList.find { it.id == 5L }
        assertThat(resultingVacation).isEqualTo(expectedVacation)
    }
}