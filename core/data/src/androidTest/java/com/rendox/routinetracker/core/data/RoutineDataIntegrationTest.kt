package com.rendox.routinetracker.core.data

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.di.completionHistoryDataModule
import com.rendox.routinetracker.core.data.di.routineDataModule
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.logic.time.generateRandomDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.testcommon.KoinTestRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.random.Random

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
            localDataSourceModule,
            routineDataModule,
            completionHistoryDataModule,
            instrumentedTestModule,
        )
    )

    @Test
    fun getInsertRoutineAndHistoryTest() = runTest {
        coroutineScheduler = testScheduler

        val routineRepository: RoutineRepository = get()
        val completionHistoryRepository: CompletionHistoryRepository = get()

        val schedule = Schedule.EveryDaySchedule(
            routineStartDate = LocalDate(2023, Month.SEPTEMBER, 1),
            vacationStartDate = LocalDate(2023, Month.SEPTEMBER, 10),
            vacationEndDate = null,
        )

        val routine = Routine.YesNoRoutine(
            id = 1,
            name = "Programming",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val history = mutableListOf<CompletionHistoryEntry>()
        val startDate = LocalDate(2023, Month.OCTOBER, 1)
        var dateCounter = startDate
        repeat(50) { index ->
            history.add(
                CompletionHistoryEntry(
                    date = dateCounter.plusDays(index),
                    status = HistoricalStatus.Completed,
                )
            )
        }
        dateCounter = dateCounter.plusDays(50)
        repeat(50) { index ->
            history.add(
                CompletionHistoryEntry(
                    date = dateCounter.plusDays(index),
                    status = HistoricalStatus.NotCompletedOnVacation,
                )
            )
        }
        dateCounter = dateCounter.plusDays(50)
        repeat(50) { index ->
            history.add(
                CompletionHistoryEntry(
                    date = dateCounter.plusDays(index),
                    status = HistoricalStatus.NotCompleted,
                )
            )
        }
        dateCounter = dateCounter.plusDays(50)

        history.forEachIndexed { index, entry ->
            completionHistoryRepository.insertHistoryEntry(
                id = null,
                routineId = routine.id!!,
                entry = entry,
                scheduleDeviationIncrementAmount = when (index) {
                    in 0..49 -> 1
                    in 50..99 -> 0
                    in 100..149 -> 0
                    else -> throw IllegalArgumentException()
                },
            )
        }

        val wholeHistory = completionHistoryRepository
            .getHistoryEntries(
                routineId = 1,
                dates = startDate..startDate.plusDays(history.lastIndex),
            )
        assertThat(wholeHistory).isEqualTo(history)

        val randomDateRange = generateRandomDateRange(
            minDate = startDate,
            maxDate = dateCounter,
        )
        val randomDateRangeIndices =
            startDate.daysUntil(randomDateRange.start)..startDate.daysUntil(randomDateRange.endInclusive)
        val randomPeriodInHistory = completionHistoryRepository
            .getHistoryEntries(
                routineId = 1,
                dates = randomDateRange,
            )
        val expectedPeriodInHistory = history.slice(randomDateRangeIndices)
        assertThat(randomPeriodInHistory).isEqualTo(expectedPeriodInHistory)

        val randomDate = startDate.plusDays(Random.nextInt(history.size))
        val singleDateRange = randomDate..randomDate
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = 1,
                dates = singleDateRange,
            )
        ).isEqualTo(listOf(history[startDate.daysUntil(randomDate)]))

        val resultingSchedule = schedule.copy(lastDateInHistory = history.last().date)
        val resultingRoutine = routineRepository.getRoutineById(routine.id!!)
        assertThat(resultingRoutine).isEqualTo(
            routine.copy(scheduleDeviation = 50, schedule = resultingSchedule)
        )
    }
}