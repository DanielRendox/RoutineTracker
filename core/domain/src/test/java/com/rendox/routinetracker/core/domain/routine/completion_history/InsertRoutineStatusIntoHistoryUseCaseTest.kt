package com.rendox.routinetracker.core.domain.routine.completion_history

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rendox.routinetracker.core.data.completion_history.completionHistoryDataModule
import com.rendox.routinetracker.core.data.routine.routineDataModule
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.localDataSourceModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

class InsertRoutineStatusIntoHistoryUseCaseTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver
    private lateinit var coroutineScheduler: TestCoroutineScheduler

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testModule = module {
        single {
            sqlDriver
        }
        single<CoroutineDispatcher> {
            UnconfinedTestDispatcher(coroutineScheduler)
        }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                localDataSourceModule,
                routineDataModule,
                completionHistoryDataModule,
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
}