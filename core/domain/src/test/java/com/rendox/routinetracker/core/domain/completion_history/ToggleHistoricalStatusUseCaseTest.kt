package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.di.completionHistoryDataModule
import com.rendox.routinetracker.core.data.di.routineDataModule
import com.rendox.routinetracker.core.data.di.streakDataModule
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.data.streak.StreakRepository
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.domain.completion_history.use_cases.ToggleHistoricalStatusUseCase
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.testcommon.fakes.routine.CompletionHistoryLocalDataSourceFake
import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineData
import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineLocalDataSourceFake
import com.rendox.routinetracker.core.testcommon.fakes.routine.StreakLocalDataSourceFake
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class ToggleHistoricalStatusUseCaseTest : KoinTest {

    private lateinit var routineRepository: RoutineRepository
    private lateinit var completionHistoryRepository: CompletionHistoryRepository
    private lateinit var toggleHistoricalStatus: ToggleHistoricalStatusUseCase
    private lateinit var streakRepository: StreakRepository

    private val testModule = module {
        single { RoutineData() }

        single<RoutineLocalDataSource> {
            RoutineLocalDataSourceFake(routineData = get())
        }

        single<CompletionHistoryLocalDataSource> {
            CompletionHistoryLocalDataSourceFake(routineData = get())
        }

        single<StreakLocalDataSource> {
            StreakLocalDataSourceFake(routineData = get())
        }
    }

    private val routineId = 1L
    private val routineStartDate = LocalDate(2023, Month.NOVEMBER, 1)
    private val routineEndDate = routineStartDate.plusDays(20)
    private val schedule = Schedule.PeriodicCustomSchedule(
        numOfDueDays = 1,
        numOfDaysInPeriod = 2,
        periodSeparationEnabled = false,
        backlogEnabled = true,
        cancelDuenessIfDoneAhead = true,
        routineStartDate = routineStartDate,
        routineEndDate = routineEndDate,
        vacationStartDate = routineStartDate.plusDays(8),
        vacationEndDate = routineStartDate.plusDays(10),
    )
    private val routine = Routine.YesNoRoutine(
        id = routineId,
        name = "",
        schedule = schedule,
    )

    private lateinit var initialHistory: List<CompletionHistoryEntry>
    private lateinit var initialStreaks: List<Streak>

    @Before
    fun setUp() = runTest {
        startKoin {
            modules(
                routineDataModule,
                completionHistoryDataModule,
                streakDataModule,
                testModule,
            )
        }

        routineRepository = get()
        completionHistoryRepository = get()
        streakRepository = get()

        toggleHistoricalStatus = ToggleHistoricalStatusUseCase(
            completionHistoryRepository = completionHistoryRepository,
            routineRepository = routineRepository,
            streakRepository = streakRepository,
        )

        routineRepository.insertRoutine(routine)

        val history = mutableListOf<CompletionHistoryEntry>()

        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(0),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(1),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(2),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(3),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(4),
                status = HistoricalStatus.AlreadyCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(6),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(7),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(8),
                status = HistoricalStatus.NotCompletedOnVacation,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(9),
                status = HistoricalStatus.SortedOutBacklogOnVacation,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(10),
                status = HistoricalStatus.OverCompletedOnVacation,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(11),
                status = HistoricalStatus.AlreadyCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(12),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(13),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(14),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(15),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(16),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(17),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(18),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(19),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        history.add(
            CompletionHistoryEntry(
                date = routineStartDate.plusDays(20),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )

        initialHistory = history
        initialStreaks = listOf(
            Streak(
                id = 1,
                startDate = routineStartDate,
                endDate = routineStartDate.plusDays(17),
            )
        )

        for (entry in history) {
            completionHistoryRepository.insertHistoryEntry(
                routineId = routineId,
                entry = entry,
            )
        }

        for (streak in initialStreaks) {
            streakRepository.insertStreak(
                routineId = routineId,
                streak = Streak(
                    startDate = streak.startDate,
                    endDate = streak.endDate,
                )
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `toggle first (Completed) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )

        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
    }

    @Test
    fun `toggle first (Completed) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newStreaks: List<Streak> = initialStreaks.toMutableList().also {
            it[0] = it[0].copy(startDate = routineStartDate.plusDays(2))
        }
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(newStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle second (Skipped) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(1)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
    }

    @Test
    fun `toggle second (Skipped) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(1)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle third (Completed) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(2)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )

        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
    }

    @Test
    fun `toggle third (Completed) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(2)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newStreaks = listOf(
            Streak(
                id = 1,
                startDate = routineStartDate,
                endDate = routineStartDate.plusDays(1),
            ),
            Streak(
                id = 2,
                startDate = routineStartDate.plusDays(3),
                endDate = routineStartDate.plusDays(17),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(newStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle forth (OverCompleted) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(3)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
    }

    @Test
    fun `toggle forth (OverCompleted) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(3)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle fifth (AlreadyCompleted) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(4)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.AlreadyCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
    }

    @Test
    fun `toggle fifth (OverCompleted) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(4)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle eighth (CompletedLater) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(7)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newHistory = mutableListOf<CompletionHistoryEntry>()
        newHistory.addAll(initialHistory)
        newHistory[7] = CompletionHistoryEntry(
            date = date,
            status = HistoricalStatus.Completed,
            scheduleDeviation = 0F,
            timesCompleted = 1F,
        )
        newHistory[9] = CompletionHistoryEntry(
            date = routineStartDate.plusDays(9),
            status = HistoricalStatus.OverCompletedOnVacation,
            scheduleDeviation = 1F,
            timesCompleted = 1F,
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(newHistory)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(initialHistory)
    }

    @Test
    fun `toggle eighth (CompletedLater) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(7)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle ninth (NotCompletedOnVacation) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(8)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.OverCompletedOnVacation,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.NotCompletedOnVacation,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
    }

    @Test
    fun `toggle ninth (NotCompletedOnVacation) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(8)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle tenth (SortedOutBacklogOnVacation) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(9)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newHistory = mutableListOf<CompletionHistoryEntry>()
        newHistory.addAll(initialHistory)
        newHistory[9] = CompletionHistoryEntry(
            date = date,
            status = HistoricalStatus.NotCompletedOnVacation,
            scheduleDeviation = 0F,
            timesCompleted = 0F,
        )
        newHistory[7] = CompletionHistoryEntry(
            date = routineStartDate.plusDays(7),
            status = HistoricalStatus.NotCompleted,
            scheduleDeviation = -1F,
            timesCompleted = 0F,
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(newHistory)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(initialHistory)
    }

    @Test
    fun `toggle tenth (SortedOutBacklogOnVacation) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(9)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newStreaks = listOf(
            Streak(
                id = 1,
                startDate = routineStartDate,
                endDate = routineStartDate.plusDays(6),
            ),
            Streak(
                id = 2,
                startDate = routineStartDate.plusDays(10),
                endDate = routineStartDate.plusDays(17),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(newStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle fifteenth (CompletedLater) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(14)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newHistory = mutableListOf<CompletionHistoryEntry>()
        newHistory.addAll(initialHistory)
        newHistory[14] = CompletionHistoryEntry(
            date = date,
            status = HistoricalStatus.Completed,
            scheduleDeviation = 0F,
            timesCompleted = 1F,
        )
        newHistory[16] = CompletionHistoryEntry(
            date = routineStartDate.plusDays(16),
            status = HistoricalStatus.OverCompleted,
            scheduleDeviation = 1F,
            timesCompleted = 1F,
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(newHistory)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(initialHistory)
    }

    @Test
    fun `toggle fifteenth (CompletedLater) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(7)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle seventeenth (SortedOutBacklog) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(16)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newHistory = mutableListOf<CompletionHistoryEntry>()
        newHistory.addAll(initialHistory)
        newHistory[16] = CompletionHistoryEntry(
            date = date,
            status = HistoricalStatus.Skipped,
            scheduleDeviation = 0F,
            timesCompleted = 0F,
        )
        newHistory[14] = CompletionHistoryEntry(
            date = routineStartDate.plusDays(14),
            status = HistoricalStatus.NotCompleted,
            scheduleDeviation = -1F,
            timesCompleted = 0F,
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(newHistory)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(initialHistory)
    }

    @Test
    fun `toggle seventeenth (SortedOutBacklog) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(16)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newStreaks = listOf(
            Streak(
                id = 1,
                startDate = routineStartDate,
                endDate = routineStartDate.plusDays(13),
            ),
            Streak(
                id = 2,
                startDate = routineStartDate.plusDays(15),
                endDate = routineStartDate.plusDays(17),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(newStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

    @Test
    fun `toggle twenty first (NotCompleted) entry back and forth, assert statuses change correctly`() = runTest {
        val date = routineStartDate.plusDays(20)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )

        assertThat(
            completionHistoryRepository.getHistoryEntryByDate(routineId, date)
        ).isEqualTo(
            CompletionHistoryEntry(
                date = date,
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
    }

    @Test
    fun `toggle twenty first (NotCompleted) entry back and forth, assert streaks change correctly`() = runTest {
        val date = routineStartDate.plusDays(20)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        val newStreaks = listOf(
            Streak(
                id = 1,
                startDate = routineStartDate,
                endDate = routineStartDate.plusDays(17),
            ),
            Streak(
                id = 2,
                startDate = routineStartDate.plusDays(20),
                endDate = null,
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(newStreaks)

        toggleHistoricalStatus(
            routineId = routineId,
            currentDate = date,
            today = routineEndDate.plusDays(1),
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(initialStreaks)
    }

//    @Test
//    fun `toggle first (completed) entry in present, assert historical entry gets deleted`() = runTest {
//        val date = routineStartDate
//
//        toggleHistoricalStatus(
//            routineId = routineId,
//            currentDate = date,
//            today = date,
//        )
//        assertThat(completionHistoryRepository.getHistoryEntryByDate(routineId, date)).isNull()
//    }
//
//    @Test
//    fun `toggle first (completed) in present entry, assert streaks change correctly`() = runTest {
//        val date = routineStartDate
//
//        toggleHistoricalStatus(
//            routineId = routineId,
//            currentDate = date,
//            today = date,
//        )
//        assertThat(streakRepository.getAllStreaks(routineId)).isEmpty()
//    }
}