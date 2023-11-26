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
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.logic.time.WeekDayNumberMonthRelated
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertFailsWith

class InsertRoutineStatusUseCaseTest : KoinTest {

    private lateinit var routineRepository: RoutineRepository
    private lateinit var completionHistoryRepository: CompletionHistoryRepository
    private lateinit var insertRoutineStatus: InsertRoutineStatusUseCase
    private lateinit var streakRepository: StreakRepository
    private val today = LocalDate(2030, Month.JANUARY, 1)

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

    @Before
    fun setUp() {
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

        insertRoutineStatus = InsertRoutineStatusUseCase(
            completionHistoryRepository = completionHistoryRepository,
            routineRepository = routineRepository,
            streakRepository = streakRepository,
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `every day schedule, standard check`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 1)

        val routineEndDate = routineStartDate.plusDays(5)

        val schedule = Schedule.EveryDaySchedule(
            routineStartDate = routineStartDate,
            vacationStartDate = routineStartDate.plusDays(3),
            vacationEndDate = routineStartDate.plusDays(3),
            routineEndDate = routineEndDate,
        )

        val expectedHistory = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 1),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.NotCompletedOnVacation,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        insertRoutineStatus(
            routineId = routineId,
            currentDate = expectedHistory[0].date,
            completedOnCurrentDate = true,
            today = today,
        )

        assertThat(
            streakRepository.getAllStreaks(routineId)
        ).isEqualTo(
            listOf(
                Streak(
                    id = 1,
                    startDate = LocalDate(2023, Month.OCTOBER, 1),
                    endDate = null,
                )
            )
        )

        insertRoutineStatus(
            routineId = routineId,
            currentDate = expectedHistory[1].date,
            completedOnCurrentDate = true,
            today = today,
        )

        assertThat(
            streakRepository.getAllStreaks(routineId)
        ).isEqualTo(
            listOf(
                Streak(
                    id = 1,
                    startDate = LocalDate(2023, Month.OCTOBER, 1),
                    endDate = null,
                )
            )
        )

        insertRoutineStatus(
            routineId = routineId,
            currentDate = expectedHistory[2].date,
            completedOnCurrentDate = false,
            today = today,
        )

        assertThat(
            streakRepository.getAllStreaks(routineId)
        ).isEqualTo(
            listOf(
                Streak(
                    id = 1,
                    startDate = LocalDate(2023, Month.OCTOBER, 1),
                    endDate = LocalDate(2023, Month.OCTOBER, 2),
                )
            )
        )

        insertRoutineStatus(
            routineId = routineId,
            currentDate = expectedHistory[3].date,
            completedOnCurrentDate = false,
            today = today,
        )

        assertThat(
            streakRepository.getAllStreaks(routineId)
        ).isEqualTo(
            listOf(
                Streak(
                    id = 1,
                    startDate = LocalDate(2023, Month.OCTOBER, 1),
                    endDate = LocalDate(2023, Month.OCTOBER, 2),
                )
            )
        )

        insertRoutineStatus(
            routineId = routineId,
            currentDate = expectedHistory[4].date,
            completedOnCurrentDate = true,
            today = today,
        )

        assertThat(
            streakRepository.getAllStreaks(routineId)
        ).isEqualTo(
            listOf(
                Streak(
                    id = 1,
                    startDate = LocalDate(2023, Month.OCTOBER, 1),
                    endDate = LocalDate(2023, Month.OCTOBER, 2),
                ),
                Streak(
                    id = 2,
                    startDate = LocalDate(2023, Month.OCTOBER, 5),
                    endDate = null,
                )
            )
        )

        insertRoutineStatus(
            routineId = routineId,
            currentDate = expectedHistory[5].date,
            completedOnCurrentDate = false,
            today = today,
        )

        assertThat(
            streakRepository.getAllStreaks(routineId)
        ).isEqualTo(
            listOf(
                Streak(
                    id = 1,
                    startDate = LocalDate(2023, Month.OCTOBER, 1),
                    endDate = LocalDate(2023, Month.OCTOBER, 2),
                ),
                Streak(
                    id = 2,
                    startDate = LocalDate(2023, Month.OCTOBER, 5),
                    endDate = LocalDate(2023, Month.OCTOBER, 5),
                )
            )
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineEndDate,
            )
        ).isEqualTo(expectedHistory)

        assertFailsWith<NullPointerException> {
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.minus(DatePeriod(days = 1)),
                completedOnCurrentDate = false,
                today = today,
            )
        }

        assertFailsWith<NullPointerException> {
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineEndDate.plus(DatePeriod(days = 1)),
                completedOnCurrentDate = false,
                today = today,
            )
        }
    }

    @Test
    fun `weekly schedule, due on explicit days of week, periodic separation enabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            routineStartDate = routineStartDate,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = true,
            periodSeparationEnabled = true,
            dueDaysOfWeek = listOf(
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ),
            startDayOfWeek = DayOfWeek.MONDAY,
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            false,  // Thursday
            false,  // Friday       + backlog
            false,  // Saturday     + backlog
            true,   // Sunday       - backlog

            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            true,   // Thursday     - backlog
            false,  // Friday       + backlog
            true,   // Saturday
            true,   // Sunday       - backlog

            false,  // Monday
            false,  // Tuesday      + backlog
        )

        val firstSixDaysOfFirstWeek = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )

        completionHistory.slice(0..5).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = firstSixDaysOfFirstWeek.first().date..firstSixDaysOfFirstWeek.last().date,
            )
        ).isEqualTo(firstSixDaysOfFirstWeek)

        val firstSixDaysOfFirstWeekExpectedStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 5),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .isEqualTo(firstSixDaysOfFirstWeekExpectedStreaks)

        insertRoutineStatus(
            routineId = routineId,
            currentDate = LocalDate(2023, Month.OCTOBER, 8),
            completedOnCurrentDate = completionHistory[6],
            today = today,
        )

        val firstWeek = mutableListOf<CompletionHistoryEntry>()
        firstWeek.addAll(firstSixDaysOfFirstWeek)
        firstWeek.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        firstWeek[5] = firstWeek[5].copy(
            status = HistoricalStatus.CompletedLater,
            scheduleDeviation = -1F,
            timesCompleted = 0F,
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstWeek.first().date..firstWeek.last().date
            )
        ).isEqualTo(firstWeek)

        val firstWeekExpectedSteaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 5),
            ),
            Streak(
                id = 2,
                startDate = LocalDate(2023, Month.OCTOBER, 7),
                endDate = null,
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId)).isEqualTo(firstWeekExpectedSteaks)

        val secondWeek = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.AlreadyCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
        )

        completionHistory.slice(7..13).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(7 + index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        val fullHistorySoFar = mutableListOf<CompletionHistoryEntry>()
        fullHistorySoFar.addAll(firstWeek)
        fullHistorySoFar.addAll(secondWeek)
        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..fullHistorySoFar.last().date
            )
        ).isEqualTo(fullHistorySoFar)

        val lastTwoDays = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 17),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )
        fullHistorySoFar.addAll(lastTwoDays)

        completionHistory.slice(14..15).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(14 + index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..fullHistorySoFar.last().date
            )
        ).isEqualTo(fullHistorySoFar)

        val fullStreakHistory = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 5),
            ),
            Streak(
                id = 2,
                startDate = LocalDate(2023, Month.OCTOBER, 7),
                endDate = LocalDate(2023, Month.OCTOBER, 16),
            )
        )

        assertThat(
            streakRepository.getAllStreaks(routineId)
        ).isEqualTo(fullStreakHistory)
    }

    @Test
    fun `weekly schedule, due on explicit days of week, periodic separation disabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
            routineStartDate = routineStartDate,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = true,
            periodSeparationEnabled = false,
            dueDaysOfWeek = listOf(
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ),
            startDayOfWeek = DayOfWeek.MONDAY,
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            false,  // Thursday
            false,  // Friday       + backlog
            false,  // Saturday     + backlog
            true,   // Sunday       - backlog

            false,  // Monday
            true,   // Tuesday
            true,   // Wednesday
            true,   // Thursday     - backlog
            false,  // Friday       + backlog
            true,   // Saturday
            true,   // Sunday       - backlog

            false,  // Monday
            false,  // Tuesday      + backlog
        )

        val firstSixDaysOfFirstWeek = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )

        completionHistory.slice(0..5).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = firstSixDaysOfFirstWeek.first().date..firstSixDaysOfFirstWeek.last().date,
            )
        ).isEqualTo(firstSixDaysOfFirstWeek)

        val firstSixDaysOfFirstWeekStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 5),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .isEqualTo(firstSixDaysOfFirstWeekStreaks)

        insertRoutineStatus(
            routineId = routineId,
            currentDate = LocalDate(2023, Month.OCTOBER, 8),
            completedOnCurrentDate = completionHistory[6],
            today = today,
        )

        val firstWeek = mutableListOf<CompletionHistoryEntry>()
        firstWeek.addAll(firstSixDaysOfFirstWeek)
        firstWeek.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            )
        )
        firstWeek[5] = firstWeek[5].copy(status = HistoricalStatus.CompletedLater)

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, firstWeek.first().date..firstWeek.last().date
            )
        ).isEqualTo(firstWeek)

        val firstWeekStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 5),
            ),
            Streak(
                id = 2,
                startDate = LocalDate(2023, Month.OCTOBER, 7),
                endDate = null,
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId)).isEqualTo(firstWeekStreaks)

        val secondWeek = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
        )

        completionHistory.slice(7..13).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(7 + index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        val fullHistorySoFar = mutableListOf<CompletionHistoryEntry>()
        fullHistorySoFar.addAll(firstWeek)
        fullHistorySoFar.addAll(secondWeek)
        fullHistorySoFar[4] = fullHistorySoFar[4].copy(status = HistoricalStatus.CompletedLater)

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..fullHistorySoFar.last().date
            )
        ).isEqualTo(fullHistorySoFar)

        val secondWeekStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = null,
            ),
        )
        assertThat(streakRepository.getAllStreaks(routineId)).isEqualTo(secondWeekStreaks)

        val lastTwoDays = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 17),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
        )
        fullHistorySoFar.addAll(lastTwoDays)

        completionHistory.slice(14..15).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(14 + index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..fullHistorySoFar.last().date,
            )
        ).isEqualTo(fullHistorySoFar)

        println(fullHistorySoFar)

        val expectedStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 16),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId)).isEqualTo(expectedStreaks)
    }

    @Test
    fun `periodic custom schedule, backlog enabled, completing ahead disabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.CustomDateSchedule(
            routineStartDate = routineStartDate,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = false,
            dueDates = listOf(
                routineStartDate.plusDays(1),
                routineStartDate.plusDays(2),
                routineStartDate.plusDays(7),
                routineStartDate.plusDays(12),
                routineStartDate.plusDays(14),
            )
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,
            true,
            false,  // + backlog
            false,
            false,
            true,   // - backlog
            false,
            true,
            false,
            true,   // + done ahead
            false,
            false,
            false,  // + backlog, because completing ahead disabled
            false,
            true,
        )

        completionHistory.slice(0..4).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        val expectedEntriesList = mutableListOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId, routineStartDate..routineStartDate.plusDays(4)
            )
        ).isEqualTo(expectedEntriesList)

        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.SortedOutBacklog,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
        )
        expectedEntriesList[2] = CompletionHistoryEntry(
            date = LocalDate(2023, Month.OCTOBER, 4),
            status = HistoricalStatus.CompletedLater,
            scheduleDeviation = -1F,
            timesCompleted = 0F,
        )

        completionHistory.slice(5..14).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(5 + index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            )
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineStartDate.plusDays(14)
            )
        ).isEqualTo(expectedEntriesList)

        val expectedStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 13),
            ),
            Streak(
                id = 2,
                startDate = LocalDate(2023, Month.OCTOBER, 16),
                endDate = null,
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId)).isEqualTo(expectedStreaks)
    }

    @Test
    fun `periodic custom schedule, backlog disabled, completing ahead enabled`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.CustomDateSchedule(
            routineStartDate = routineStartDate,
            backlogEnabled = false,
            cancelDuenessIfDoneAhead = true,
            dueDates = listOf(
                routineStartDate.plusDays(1),
                routineStartDate.plusDays(2),
                routineStartDate.plusDays(7),
                routineStartDate.plusDays(12),
                routineStartDate.plusDays(14),
            )
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = listOf(
            false,
            true,
            false,  // not completed but no backlog, because it's disabled
            false,
            false,
            true,   // + done ahead
            false,
            true,
            false,
            true,   // + done ahead
            false,
            false,
            false,  // - done ahead
            false,
            true,
        )

        completionHistory.slice(0..14).forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        val expectedEntriesList = listOf(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 2),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 3),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 4),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 5),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 7),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 8),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.OverCompleted,
                scheduleDeviation = 1F,
                timesCompleted = 1F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 12),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 13),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 14),
                status = HistoricalStatus.AlreadyCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 15),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            ),
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 16),
                status = HistoricalStatus.Completed,
                scheduleDeviation = 0F,
                timesCompleted = 1F,
            ),
        )

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId,
                routineStartDate..routineStartDate.plusDays(14)
            )
        ).isEqualTo(expectedEntriesList)

        val expectedStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 2),
                endDate = LocalDate(2023, Month.OCTOBER, 3),
            ),
            Streak(
                id = 2,
                startDate = LocalDate(2023, Month.OCTOBER, 7),
                endDate = null,
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId)).isEqualTo(expectedStreaks)
    }

    @Test
    fun `monthly schedule, not completed for a long time`() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday

        val schedule = Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = listOf(1, 6, 9, 11, 26, 30),
            includeLastDayOfMonth = true,
            weekDaysMonthRelated = listOf(
                WeekDayMonthRelated(DayOfWeek.TUESDAY, WeekDayNumberMonthRelated.Third),
                WeekDayMonthRelated(DayOfWeek.TUESDAY, WeekDayNumberMonthRelated.Forth),
                WeekDayMonthRelated(DayOfWeek.THURSDAY, WeekDayNumberMonthRelated.Fifth),
            ),
            startFromRoutineStart = false,
            periodSeparationEnabled = false,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = true,
            routineStartDate = routineStartDate,
            vacationStartDate = LocalDate(2023, Month.OCTOBER, 16),
            vacationEndDate = LocalDate(2023, Month.OCTOBER, 22),
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val completionHistory = mutableListOf<Boolean>()
        repeat(17) { completionHistory.add(false) }
        repeat(2) { completionHistory.add(true) }
        repeat(15) { completionHistory.add(false) }
        completionHistory.forEachIndexed { index, isCompleted ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = isCompleted,
                today = today,
            )
        }

        val expectedEntriesList = mutableListOf<CompletionHistoryEntry>()
        repeat(4) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 2),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 6),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        repeat(2) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 7),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 9),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 10),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 11),
                status = HistoricalStatus.CompletedLater,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        repeat(4) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 12),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 16),
                    status = HistoricalStatus.NotCompletedOnVacation,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(2) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 19),
                    status = HistoricalStatus.SortedOutBacklogOnVacation,
                    scheduleDeviation = 1F,
                    timesCompleted = 1F,
                )
            )
        }
        repeat(2) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 21),
                    status = HistoricalStatus.NotCompletedOnVacation,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 23),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 24),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 25),
                status = HistoricalStatus.Skipped,
                scheduleDeviation = 0F,
                timesCompleted = 0F,
            )
        )
        expectedEntriesList.add(
            CompletionHistoryEntry(
                date = LocalDate(2023, Month.OCTOBER, 26),
                status = HistoricalStatus.NotCompleted,
                scheduleDeviation = -1F,
                timesCompleted = 0F,
            )
        )
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = LocalDate(2023, Month.OCTOBER, it + 27),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = routineStartDate.plusDays(it + 28),
                    status = HistoricalStatus.NotCompleted,
                    scheduleDeviation = -1F,
                    timesCompleted = 0F,
                )
            )
        }
        repeat(3) {
            expectedEntriesList.add(
                CompletionHistoryEntry(
                    date = routineStartDate.plusDays(it + 31),
                    status = HistoricalStatus.Skipped,
                    scheduleDeviation = 0F,
                    timesCompleted = 0F,
                )
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId,
                routineStartDate..routineStartDate.plusDays(33)
            )
        ).isEqualTo(expectedEntriesList)

        val expectedStreaks = listOf(
            Streak(
                id = 1,
                startDate = LocalDate(2023, Month.OCTOBER, 9),
                endDate = LocalDate(2023, Month.OCTOBER, 23),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId)).isEqualTo(expectedStreaks)
    }

    @Test
    fun testPeriodicCustomSchedule() = runTest {
        val routineId = 1L
        val routineStartDate = LocalDate(2023, Month.NOVEMBER, 1)

        val schedule = Schedule.PeriodicCustomSchedule(
            numOfDueDays = 1,
            numOfDaysInPeriod = 2,
            periodSeparationEnabled = false,
            backlogEnabled = true,
            cancelDuenessIfDoneAhead = true,
            routineStartDate = routineStartDate,
            vacationStartDate = routineStartDate.plusDays(8),
            vacationEndDate = routineStartDate.plusDays(10),
        )

        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = schedule,
        )

        routineRepository.insertRoutine(routine)

        val history = mutableListOf<CompletionHistoryEntry>()

        history.add(
            CompletionHistoryEntry(
                date = routineStartDate,
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

        val completion = listOf(
            true,
            false,
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            true,
            true,
            false,
            false,
            false,
            false,
            true,
            true,
            false,
            false,
            false,
            false,
        )

        val today = routineStartDate.plusDays(22)
        completion.forEachIndexed { index, completed ->
            insertRoutineStatus(
                routineId = routineId,
                currentDate = routineStartDate.plusDays(index),
                completedOnCurrentDate = completed,
                today = today,
            )
        }

        assertThat(
            completionHistoryRepository.getHistoryEntries(
                routineId = routineId,
                dates = routineStartDate..routineStartDate.plusDays(21),
            )
        ).containsExactlyElementsIn(history)

        val expectedStreaks = listOf(
            Streak(
                id = 1,
                startDate = routineStartDate,
                endDate = routineStartDate.plusDays(17),
            )
        )
        assertThat(streakRepository.getAllStreaks(routineId))
            .containsExactlyElementsIn(expectedStreaks)
    }
}