package com.rendox.routinetracker

import com.rendox.routinetracker.routine.data.RoutineRepositoryImpl
import com.rendox.routinetracker.routine.di.routineDataModule
import com.rendox.routinetracker.routine.model.RoutineType
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import com.google.common.truth.Truth.assertThat
import com.rendox.performancetracker.feature.routine.Routine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.dsl.module

class RoutineViewModelTest : KoinTest {

    @get:Rule
    val koinTestRule = com.rendox.routinetracker.KoinTestRule(
        // order matters — parent goes first
        modules = listOf(
            dataModule,
            routineDataModule,
        )
    )

    @Test
    fun populateDatabaseOnInitialization() = runBlocking {
        val routineRepository = RoutineRepositoryImpl(localDataSource = get())

        val routineType = RoutineType.YesNoRoutine
        val routineName = "Do sports"
        val routineStartDate = LocalDate(2023, 9, 17)
        val routineBacklogEnabled = true
        val routinePeriodSeparation = true
        val routineId: Long = 1

        routineRepository.insertRoutine(
            id = routineId,
            type = routineType,
            name = routineName,
            startDate = routineStartDate,
            backlogEnabled = routineBacklogEnabled,
            periodSeparation = routinePeriodSeparation,
        )

        val viewModel = RoutineViewModel(routineRepository, routineId = routineId)

        // Wait before the viewModel fetches data
        delay(2000)

        val routineScreenState = viewModel.routineScreenState.value
        assertThat(routineScreenState).isEqualTo(RoutineScreenState(
            routineName = routineName,
            routineStartDate = routineStartDate.toString(),
        ))
    }
}