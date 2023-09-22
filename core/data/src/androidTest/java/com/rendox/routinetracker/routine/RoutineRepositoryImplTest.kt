package com.rendox.routinetracker.routine

import com.google.common.truth.Truth
import com.rendox.performancetracker.feature.routine.Routine
import com.rendox.routinetracker.dataModule
import com.rendox.routinetracker.routine.data.RoutineRepository
import com.rendox.routinetracker.routine.di.routineDataModule
import com.rendox.routinetracker.routine.model.RoutineType
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

class RoutineRepositoryImplTest : KoinTest {

    private lateinit var coroutineScheduler: TestCoroutineScheduler

    // this class overrides dependencies in globalModule
    @OptIn(ExperimentalCoroutinesApi::class)
    private val instrumentedTestModule = module {
        single<CoroutineDispatcher> {
            UnconfinedTestDispatcher(coroutineScheduler)
        }
    }

    @get:Rule
    val koinTestRule = com.rendox.routinetracker.KoinTestRule(
        // order matters — parent goes first
        modules = listOf(
            dataModule,
            routineDataModule,
            instrumentedTestModule
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

        val routineRepository: RoutineRepository = get()

        routineRepository.insertRoutine(
            type = routineType,
            name = routineName,
            startDate = routineStartDate,
            backlogEnabled = routineBacklogEnabled,
            periodSeparation = routinePeriodSeparation,
        )

        val result = routineRepository.getRoutineById(routineId)

        Truth.assertThat(result).isEqualTo(
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