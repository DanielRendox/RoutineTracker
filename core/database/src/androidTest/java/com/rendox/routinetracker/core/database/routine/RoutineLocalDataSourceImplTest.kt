package com.rendox.routinetracker.core.database.routine

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.database.dataModule
import com.rendox.routinetracker.core.model.RoutineType
import com.rendox.routinetracker.core.testcommon.KoinTestRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class RoutineLocalDataSourceImplTest : KoinTest {

    private lateinit var coroutineScheduler: TestCoroutineScheduler

    // this class overrides dependencies in globalModule
    @OptIn(ExperimentalCoroutinesApi::class)
    private val instrumentedTestModule = module {
        single<CoroutineDispatcher> {
            UnconfinedTestDispatcher(coroutineScheduler)
        }
    }

    @get:Rule
    val koinTestRule = KoinTestRule(
        // order matters — parent goes first
        modules = listOf(
            dataModule,
            instrumentedTestModule,
        )
    )

    @Test
    fun routineGetInsert() = runTest {
        coroutineScheduler = testScheduler

        val routineType = RoutineType.YesNoRoutine
        val routineName = "Do sports"
        val routineStartDate = LocalDate(2023, 9, 17)
        val routineBacklogEnabled = true
        val routinePeriodSeparation = true
        val routineId: Long = 1

        val dataSource: RoutineLocalDataSource = RoutineLocalDataSourceImpl(
            db = get(),
            dispatcher = get(),
        )

        dataSource.insertRoutine(
            type = routineType,
            name = routineName,
            startDate = routineStartDate,
            backlogEnabled = routineBacklogEnabled,
            periodSeparation = routinePeriodSeparation,
        )

        val result = dataSource.getRoutineById(routineId)

        assertThat(result).isEqualTo(
            Routine(
                id = routineId,
                type = routineType,
                name = routineName,
                startDate = routineStartDate,
                backlogEnabled = routineBacklogEnabled,
                periodSeparation = routinePeriodSeparation,
                vacationStartDate = null,
                vacationEndDate = null,
            )
        )
    }
}