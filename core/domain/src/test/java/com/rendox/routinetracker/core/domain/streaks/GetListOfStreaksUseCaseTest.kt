//package com.rendox.routinetracker.core.domain.streaks
//
//import com.google.common.truth.Truth.assertThat
//import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
//import com.rendox.routinetracker.core.data.di.completionHistoryDataModule
//import com.rendox.routinetracker.core.data.di.routineDataModule
//import com.rendox.routinetracker.core.data.routine.RoutineRepository
//import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
//import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
//import com.rendox.routinetracker.core.logic.time.plusDays
//import com.rendox.routinetracker.core.logic.time.rangeTo
//import com.rendox.routinetracker.core.model.CompletionHistoryEntry
//import com.rendox.routinetracker.core.model.HistoricalStatus
//import com.rendox.routinetracker.core.model.Routine
//import com.rendox.routinetracker.core.model.Schedule
//import com.rendox.routinetracker.core.model.Streak
//import com.rendox.routinetracker.core.testcommon.fakes.routine.CompletionHistoryLocalDataSourceFake
//import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineData
//import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineLocalDataSourceFake
//import kotlinx.coroutines.test.runTest
//import kotlinx.datetime.DayOfWeek
//import kotlinx.datetime.LocalDate
//import kotlinx.datetime.Month
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.koin.core.context.startKoin
//import org.koin.core.context.stopKoin
//import org.koin.dsl.module
//import org.koin.test.KoinTest
//import org.koin.test.get
//
//class GetListOfStreaksUseCaseTest : KoinTest {
//
//    private lateinit var getListOfStreaks: GetListOfStreaksUseCase
//    private lateinit var completionHistoryRepository: CompletionHistoryRepository
//    private lateinit var routineRepository: RoutineRepository
//
//    private val everyDayScheduleRoutineId = 1L
//    private val defaultRoutineStartDate = LocalDate(2023, Month.OCTOBER, 1)
//
//    private val testModule = module {
//        single {
//            RoutineData()
//        }
//
//        single<RoutineLocalDataSource> {
//            RoutineLocalDataSourceFake(routineData = get())
//        }
//
//        single<CompletionHistoryLocalDataSource> {
//            CompletionHistoryLocalDataSourceFake(routineData = get())
//        }
//    }
//
//    @Before
//    fun setUp() = runTest {
//        startKoin {
//            modules(
//                routineDataModule,
//                completionHistoryDataModule,
//                testModule,
//            )
//        }
//
//        val schedule = Schedule.EveryDaySchedule(routineStartDate = defaultRoutineStartDate)
//        val routine = Routine.YesNoRoutine(
//            id = everyDayScheduleRoutineId, name = "", schedule = schedule
//        )
//
//        routineRepository = get()
//        routineRepository.insertRoutine(routine)
//
//        getListOfStreaks = GetListOfStreaksUseCase(
//            completionHistoryRepository = get(),
//            routineRepository = routineRepository,
//        )
//
//        completionHistoryRepository = get()
//    }
//
//    @After
//    fun tearDown() {
//        stopKoin()
//    }
//
//    @Test
//    fun `all completed or skipped, assert returns one endless streak`() = runTest {
//        val history = listOf(
//            HistoricalStatus.Completed,
//            HistoricalStatus.Completed,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Completed,
//            HistoricalStatus.NotCompletedOnVacation,
//            HistoricalStatus.OverCompletedOnVacation,
//            HistoricalStatus.OverCompleted,
//            HistoricalStatus.AlreadyCompleted,
//            HistoricalStatus.Completed,
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = everyDayScheduleRoutineId,
//                entry = CompletionHistoryEntry(
//                    date = defaultRoutineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        val expectedStreaks = listOf(Streak(start = defaultRoutineStartDate, end = null))
//        assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(expectedStreaks)
//    }
//
//    @Test
//    fun `all not completed or skipped, assert returns an empty list`() = runTest {
//        val history = listOf(
//            HistoricalStatus.NotCompleted,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.NotCompletedOnVacation,
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = everyDayScheduleRoutineId,
//                entry = CompletionHistoryEntry(
//                    date = defaultRoutineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(emptyList<Streak>())
//    }
//
//    @Test
//    fun `completed and not completed, no backlog, assert returns correct streaks`() = runTest {
//        val history = listOf(
//            HistoricalStatus.Completed,
//            HistoricalStatus.Completed,
//
//            HistoricalStatus.NotCompleted,
//            HistoricalStatus.Skipped,
//
//            HistoricalStatus.Completed,
//            HistoricalStatus.NotCompletedOnVacation,
//            HistoricalStatus.OverCompletedOnVacation,
//
//            HistoricalStatus.NotCompleted,
//
//            HistoricalStatus.OverCompleted,
//            HistoricalStatus.AlreadyCompleted,
//
//            HistoricalStatus.NotCompleted,
//
//            HistoricalStatus.Completed,
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = everyDayScheduleRoutineId,
//                entry = CompletionHistoryEntry(
//                    date = defaultRoutineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        val expectedStreaks = listOf(
//            Streak(start = defaultRoutineStartDate, end = defaultRoutineStartDate.plusDays(1)),
//            Streak(
//                start = defaultRoutineStartDate.plusDays(4),
//                end = defaultRoutineStartDate.plusDays(6)
//            ),
//            Streak(
//                start = defaultRoutineStartDate.plusDays(8),
//                end = defaultRoutineStartDate.plusDays(9)
//            ),
//            Streak(start = defaultRoutineStartDate.plusDays(11), end = null),
//        )
//
//        assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(expectedStreaks)
//    }
//
//    @Test
//    fun `completed and not completed + backlog, assert returns correct streaks`() = runTest {
//        val history = listOf(
//            HistoricalStatus.Completed,
//            HistoricalStatus.Completed,
//            HistoricalStatus.CompletedLater,
//            HistoricalStatus.CompletedLater,
//            HistoricalStatus.SortedOutBacklog,
//            HistoricalStatus.NotCompletedOnVacation,
//            HistoricalStatus.SortedOutBacklogOnVacation,
//
//            HistoricalStatus.NotCompleted,
//
//            HistoricalStatus.Completed,
//            HistoricalStatus.OverCompleted,
//            HistoricalStatus.AlreadyCompleted,
//            HistoricalStatus.Completed,
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = everyDayScheduleRoutineId,
//                entry = CompletionHistoryEntry(
//                    date = defaultRoutineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        val expectedStreaks = listOf(
//            Streak(start = defaultRoutineStartDate, end = defaultRoutineStartDate.plusDays(6)),
//            Streak(start = defaultRoutineStartDate.plusDays(8), end = null)
//        )
//
//        assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(expectedStreaks)
//    }
//
//    @Test
//    fun `skipped for some time after start date, then sorted out backlog, assert counts streak from start date`() =
//        runTest {
//            val history = listOf(
//                HistoricalStatus.Skipped,
//                HistoricalStatus.Skipped,
//                HistoricalStatus.CompletedLater,
//                HistoricalStatus.Skipped,
//                HistoricalStatus.NotCompletedOnVacation,
//                HistoricalStatus.SortedOutBacklogOnVacation,
//
//                HistoricalStatus.NotCompleted,
//                HistoricalStatus.NotCompleted,
//            )
//
//            history.forEachIndexed { index, status ->
//                completionHistoryRepository.insertHistoryEntry(
//                    routineId = everyDayScheduleRoutineId,
//                    entry = CompletionHistoryEntry(
//                        date = defaultRoutineStartDate.plusDays(index),
//                        status = status,
//                    ),
//                    scheduleDeviationIncrementAmount = 0,
//                )
//            }
//
//            val expectedStreaks = listOf(
//                Streak(start = defaultRoutineStartDate, end = defaultRoutineStartDate.plusDays(5))
//            )
//            assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(expectedStreaks)
//        }
//
//    @Test
//    fun `skipped for some time after start date, then completed, assert counts streak from start date`() =
//        runTest {
//            val history = listOf(
//                HistoricalStatus.Skipped,
//                HistoricalStatus.Skipped,
//                HistoricalStatus.Skipped,
//                HistoricalStatus.NotCompletedOnVacation,
//                HistoricalStatus.Completed,
//                HistoricalStatus.Skipped,
//
//                HistoricalStatus.NotCompleted,
//                HistoricalStatus.NotCompleted
//            )
//
//            history.forEachIndexed { index, status ->
//                completionHistoryRepository.insertHistoryEntry(
//                    routineId = everyDayScheduleRoutineId,
//                    entry = CompletionHistoryEntry(
//                        date = defaultRoutineStartDate.plusDays(index),
//                        status = status,
//                    ),
//                    scheduleDeviationIncrementAmount = 0,
//                )
//            }
//
//            val expectedStreaks = listOf(
//                Streak(start = defaultRoutineStartDate, end = defaultRoutineStartDate.plusDays(5))
//            )
//            assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(expectedStreaks)
//        }
//
//    @Test
//    fun `skipped for some time after start date and then never completed, assert returns empty list`() =
//        runTest {
//            val history = listOf(
//                HistoricalStatus.Skipped,
//                HistoricalStatus.Skipped,
//                HistoricalStatus.Skipped,
//                HistoricalStatus.NotCompletedOnVacation,
//                HistoricalStatus.NotCompletedOnVacation,
//                HistoricalStatus.Skipped,
//                HistoricalStatus.NotCompleted,
//                HistoricalStatus.NotCompleted,
//            )
//
//            history.forEachIndexed { index, status ->
//                completionHistoryRepository.insertHistoryEntry(
//                    routineId = everyDayScheduleRoutineId,
//                    entry = CompletionHistoryEntry(
//                        date = defaultRoutineStartDate.plusDays(index),
//                        status = status,
//                    ),
//                    scheduleDeviationIncrementAmount = 0,
//                )
//            }
//
//            assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(emptyList<HistoricalStatus>())
//        }
//
//    @Test
//    fun `assert streak does not continue until all backlog is sorted out`() = runTest {
//        val history = listOf(
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Completed,
//            HistoricalStatus.CompletedLater,
//
//            HistoricalStatus.NotCompleted,
//            HistoricalStatus.Skipped,  // not a streak start, because there's still backlog
//            HistoricalStatus.Skipped,
//
//            HistoricalStatus.Completed,     // new streak
//            HistoricalStatus.OverCompleted,
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = everyDayScheduleRoutineId,
//                entry = CompletionHistoryEntry(
//                    date = defaultRoutineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        val expectedStreaks = listOf(
//            Streak(
//                start = defaultRoutineStartDate, end = defaultRoutineStartDate.plusDays(2)
//            ),
//            Streak(
//                start = defaultRoutineStartDate.plusDays(6), end = null
//            )
//        )
//        assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(expectedStreaks)
//    }
//
//    @Test
//    fun `assert streak continues when all backlog is sorted out`() = runTest {
//        val history = listOf(
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Completed,
//            HistoricalStatus.CompletedLater,
//            HistoricalStatus.CompletedLater,
//            HistoricalStatus.SortedOutBacklog,
//            HistoricalStatus.SortedOutBacklogOnVacation,
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = everyDayScheduleRoutineId,
//                entry = CompletionHistoryEntry(
//                    date = defaultRoutineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        val expectedStreaks = listOf(
//            Streak(start = defaultRoutineStartDate, end = null)
//        )
//        assertThat(getListOfStreaks(everyDayScheduleRoutineId)).isEqualTo(expectedStreaks)
//    }
//
//    @Test
//    fun `streak starts from period start when period separation enabled and completed later on`() = runTest {
//        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday
//
//        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
//            dueDaysOfWeek = listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
//            startDayOfWeek = routineStartDate.dayOfWeek,
//            periodSeparationEnabled = true,
//            backlogEnabled = true,
//            cancelDuenessIfDoneAhead = true,
//            routineStartDate = routineStartDate,
//        )
//
//        val routineId = 2L
//        val routine = Routine.YesNoRoutine(
//            id = routineId, name = "", schedule = schedule
//        )
//        routineRepository.insertRoutine(routine)
//
//        val history = listOf(
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.NotCompleted,
//            HistoricalStatus.NotCompleted,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.NotCompleted,
//
//            HistoricalStatus.Skipped,       // streak starts here because of period separation
//            HistoricalStatus.Skipped,       // even though there is a backlog
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Completed,     // defines a streak
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = routineId,
//                entry = CompletionHistoryEntry(
//                    date = routineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        val expectedStreaks = listOf(
//            Streak(start = routineStartDate.plusDays(7), end = null)
//        )
//        assertThat(getListOfStreaks(routineId)).isEqualTo(expectedStreaks)
//    }
//
//    @Test
//    fun `streak doesn't from period start when period separation enabled but not completed later on`() = runTest {
//        val routineStartDate = LocalDate(2023, Month.OCTOBER, 2) // Monday
//
//        val schedule = Schedule.WeeklyScheduleByDueDaysOfWeek(
//            dueDaysOfWeek = listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
//            startDayOfWeek = routineStartDate.dayOfWeek,
//            periodSeparationEnabled = true,
//            backlogEnabled = true,
//            cancelDuenessIfDoneAhead = true,
//            routineStartDate = routineStartDate,
//        )
//
//        val routineId = 2L
//        val routine = Routine.YesNoRoutine(
//            id = routineId, name = "", schedule = schedule
//        )
//        routineRepository.insertRoutine(routine)
//
//        val history = listOf(
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.NotCompleted,
//            HistoricalStatus.Skipped, // not a streak start because it's not the start of the period
//
//            HistoricalStatus.Completed,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.NotCompleted,
//
//            HistoricalStatus.Skipped,       // not streak start
//            HistoricalStatus.Skipped,
//            HistoricalStatus.CompletedLater,
//
//            HistoricalStatus.SortedOutBacklog,
//            HistoricalStatus.Completed,
//
//            HistoricalStatus.NotCompleted,
//            HistoricalStatus.Skipped,
//
//            HistoricalStatus.Skipped,       // not a streak start
//            HistoricalStatus.Skipped,
//            HistoricalStatus.Skipped,
//            HistoricalStatus.NotCompletedOnVacation,
//            HistoricalStatus.AlreadyCompleted,
//        )
//
//        history.forEachIndexed { index, status ->
//            completionHistoryRepository.insertHistoryEntry(
//                routineId = routineId,
//                entry = CompletionHistoryEntry(
//                    date = routineStartDate.plusDays(index),
//                    status = status,
//                ),
//                scheduleDeviationIncrementAmount = 0,
//            )
//        }
//
//        val expectedStreaks = listOf(
//            Streak(
//                start = routineStartDate.plusDays(5),
//                end = routineStartDate.plusDays(6)
//            ),
//            Streak(
//                start = routineStartDate.plusDays(11),
//                end = routineStartDate.plusDays(12),
//            ),
//        )
//        assertThat(getListOfStreaks(routineId)).isEqualTo(expectedStreaks)
//    }
//
//    @Test
//    fun `one endless streak, derive streak dates from DatePeriod, not exceeding streak start`() {
//        val streak = Streak(start = LocalDate(2023, Month.OCTOBER, 1), end = null)
//        val streakList = listOf(streak)
//        val dateRange =
//            LocalDate(2023, Month.OCTOBER, 10)..LocalDate(2023, Month.OCTOBER, 30)
//        val result = deriveDatesIncludedInStreak(streakList, dateRange)
//        val expectedDatesIncludedInStreak = mutableListOf<LocalDate>()
//        for (streakDate in dateRange) expectedDatesIncludedInStreak.add(streakDate)
//        assertThat(result).isEqualTo(expectedDatesIncludedInStreak)
//    }
//
//    @Test
//    fun `one endless streak, derive streak dates from DatePeriod, exceeding streak start`() {
//        val streak = Streak(start = LocalDate(2023, Month.OCTOBER, 1), end = null)
//        val streakList = listOf(streak)
//        val dateRange =
//            LocalDate(2023, Month.SEPTEMBER, 10)..LocalDate(2023, Month.OCTOBER, 30)
//        val streakDateRange =
//            LocalDate(2023, Month.OCTOBER, 1)..LocalDate(2023, Month.OCTOBER, 30)
//        val result = deriveDatesIncludedInStreak(streakList, dateRange)
//        val expectedDatesIncludedInStreak = mutableListOf<LocalDate>()
//        for (streakDate in streakDateRange) expectedDatesIncludedInStreak.add(streakDate)
//        assertThat(result).isEqualTo(expectedDatesIncludedInStreak)
//    }
//
//    @Test
//    fun `empty streak list, derive streak dates from DatePeriod`() {
//        val streakList = emptyList<Streak>()
//        val dateRange =
//            LocalDate(2023, Month.SEPTEMBER, 10)..LocalDate(2023, Month.OCTOBER, 30)
//        assertThat(deriveDatesIncludedInStreak(streakList, dateRange)).isEmpty()
//    }
//
//    @Test
//    fun `multiple streaks within given DatePeriod, derive streak dates from DatePeriod`() {
//        val streaks = listOf(
//            Streak(
//                start = LocalDate(2022, Month.JANUARY, 1),
//                end = LocalDate(2022, Month.JANUARY, 5),
//            ),
//            Streak(
//                start = LocalDate(2023, Month.OCTOBER, 2),
//                end = LocalDate(2023, Month.OCTOBER, 4),
//            ),
//            Streak(
//                start = LocalDate(2023, Month.OCTOBER, 15),
//                end = LocalDate(2023, Month.OCTOBER, 15),
//            ),
//            Streak(
//                start = LocalDate(2023, Month.OCTOBER, 30),
//                end = LocalDate(2023, Month.NOVEMBER, 2),
//            ),
//            Streak(
//                start = LocalDate(2024, Month.JANUARY, 1),
//                end = LocalDate(2024, Month.JANUARY, 5),
//            )
//        )
//        val dateRange =
//            LocalDate(2023, Month.OCTOBER, 1)..LocalDate(2023, Month.OCTOBER, 30)
//        val expectedDatesIncludedInStreak = listOf(
//            LocalDate(2023, Month.OCTOBER, 2),
//            LocalDate(2023, Month.OCTOBER, 3),
//            LocalDate(2023, Month.OCTOBER, 4),
//            LocalDate(2023, Month.OCTOBER, 15),
//            LocalDate(2023, Month.OCTOBER, 30),
//        )
//        val result = deriveDatesIncludedInStreak(streaks, dateRange)
//        assertThat(result).isEqualTo(expectedDatesIncludedInStreak)
//    }
//}