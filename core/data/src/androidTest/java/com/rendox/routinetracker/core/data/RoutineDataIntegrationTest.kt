package com.rendox.routinetracker.core.data

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.completion_history.completionHistoryModule
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.data.routine.routineDataModule
import com.rendox.routinetracker.core.database.dataModule
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.testcommon.KoinTestRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class RoutineDataIntegrationTest : KoinTest {

    private lateinit var coroutineScheduler: TestCoroutineScheduler

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
            routineDataModule,
            completionHistoryModule,
            instrumentedTestModule,
        )
    )

    data class HistoryEntry(
        val numberOfDateFromRoutineStart: Long,
        val routineId: Long,
        val status: HistoricalStatus,
    )

    @Test
    fun getInsertRoutineAndHistoryTest() = runTest {
        coroutineScheduler = testScheduler

        val routineRepository: RoutineRepository = get()
        val completionHistoryRepository: CompletionHistoryRepository = get()

        val schedule = Schedule.WeeklySchedule(
            dueDaysOfWeek = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY
            ),
            startDayOfWeek = DayOfWeek.MONDAY,
        )

        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Do sports",
            startDate = LocalDate(2023, Month.SEPTEMBER, 28),
            backlogEnabled = false,
            periodSeparation = true,
            vacationStartDate = null,
            vacationEndDate = null,
            schedule = schedule,
        )

        val history = listOf(
            HistoryEntry(
                numberOfDateFromRoutineStart = 1,
                routineId = routine.id!!,
                status = HistoricalStatus.FullyCompleted,
            ),
            HistoryEntry(
                numberOfDateFromRoutineStart = 2,
                routineId = routine.id!!,
                status = HistoricalStatus.NotCompleted,
            ),
            HistoryEntry(
                numberOfDateFromRoutineStart = 3,
                routineId = routine.id!!,
                status = HistoricalStatus.FullyCompleted,
            ),
        )

        routineRepository.insertRoutine(routine)
        for (historyEntry in history) {
            completionHistoryRepository.insertHistoryEntry(
                numberOfDateFromRoutineStart = historyEntry.numberOfDateFromRoutineStart,
                routineId = historyEntry.routineId,
                status = historyEntry.status,
            )
        }

        val resultingRoutine = routineRepository.getRoutineById(routine.id!!)
        val resultingHistory = completionHistoryRepository.getHistoryEntriesByIndices(
            routineId = routine.id!!,
            dateFromRoutineStartIndices = 1L..3L,
        )
        assertThat(resultingRoutine).isEqualTo(routine)
        assertThat(resultingHistory.first()).isEqualTo(history.map { it.status })
    }
}