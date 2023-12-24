package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.routine_completion_history.RoutineCompletionHistoryRepository
import com.rendox.routinetracker.core.data.di.completionHistoryDataModule
import com.rendox.routinetracker.core.data.di.routineDataModule
import com.rendox.routinetracker.core.data.di.streakDataModule
import com.rendox.routinetracker.core.data.routine.HabitRepository
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.routine.HabitLocalDataSource
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.domain.completion_history.use_cases.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.di.streakDomainModule
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.testcommon.fakes.habit.CompletionHistoryLocalDataSourceFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitData
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitLocalDataSourceFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.StreakLocalDataSourceFake
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.random.Random
import kotlin.random.nextInt

class GetHabitStatusUseCaseTest : KoinTest {

    private lateinit var insertRoutineStatusIntoHistory: InsertRoutineStatusUseCase
    private lateinit var getRoutineStatusList: GetRoutineStatusUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var routineCompletionHistoryRepository: RoutineCompletionHistoryRepository

    private val routineId = 1L
    private val routineStartDate = LocalDate(2023, Month.OCTOBER, 11)
    private val routineEndDate = LocalDate(2023, Month.NOVEMBER, 12)

    private var weeklyScheduleByNumOfDueDays = Schedule.WeeklyScheduleByNumOfDueDays(
        numOfDueDays = 5,
        numOfDueDaysInFirstPeriod = 4,
        startDayOfWeek = DayOfWeek.MONDAY,
        backlogEnabled = true,
        completingAheadEnabled = true,
        startDate = routineStartDate,
        endDate = routineEndDate,
    )


    private val testModule = module {
        single {
            HabitData()
        }

        single<HabitLocalDataSource> {
            HabitLocalDataSourceFake(habitData = get())
        }

        single<CompletionHistoryLocalDataSource> {
            CompletionHistoryLocalDataSourceFake(habitData = get())
        }

        single<StreakLocalDataSource> {
            StreakLocalDataSourceFake(habitData = get())
        }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                routineDataModule,
                completionHistoryDataModule,
                streakDataModule,
                streakDomainModule,
                testModule,
            )
        }

        habitRepository = get()
        routineCompletionHistoryRepository = get()

        insertRoutineStatusIntoHistory = InsertRoutineStatusUseCase(
            routineCompletionHistoryRepository = get(),
            habitRepository = get(),
            startStreakOrJoinStreaks = get(),
            breakStreak = get(),
        )

        getRoutineStatusList = GetRoutineStatusUseCase(
            habitRepository = get(),
            routineCompletionHistoryRepository = get(),
            insertRoutineStatus = insertRoutineStatusIntoHistory,
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `get routine status for dates prior to routine start, returns empty list`() = runTest {
        val habit = Habit.YesNoHabit(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        habitRepository.insertHabit(habit)

        val randomDateBeforeRoutineStart =
            routineStartDate.minus(DatePeriod(days = Random.nextInt(1..50)))

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = randomDateBeforeRoutineStart..routineStartDate.minus(DatePeriod(days = 1)),
                today = LocalDate(2022, Month.NOVEMBER, 19),
            )
        ).isEqualTo(emptyList<StatusEntry>())
    }

    @Test
    fun `WeeklyScheduleByNumOfDueDays, test due not due`() = runTest {
        val habit = Habit.YesNoHabit(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        habitRepository.insertHabit(habit)

        val expectedStatuses = mutableListOf<PlanningStatus>()
        repeat(4) { expectedStatuses.add(PlanningStatus.Planned) }
        expectedStatuses.add(PlanningStatus.NotDue)
        repeat(5) { expectedStatuses.add(PlanningStatus.Planned) }
        repeat(2) { expectedStatuses.add(PlanningStatus.NotDue) }

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = routineStartDate..LocalDate(2023, Month.OCTOBER, 22),
                today = routineStartDate,
            )
        ).isEqualTo(
            expectedStatuses.mapIndexed { index, status ->
                StatusEntry(routineStartDate.plusDays(index), status)
            }
        )
        assertThat(habitRepository.getHabitById(routineId)).isEqualTo(habit)
    }

    @Test
    fun `WeeklyScheduleByNumOfDueDays, test history pre-population`() = runTest {
        val habit = Habit.YesNoHabit(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        habitRepository.insertHabit(habit)

        val thirdWeekPeriod =
            LocalDate(2023, Month.OCTOBER, 23)..LocalDate(2023, Month.OCTOBER, 29)

        val expectedStatusesOnThirdWeek = mutableListOf<RoutineStatus>()
        repeat(2) { expectedStatusesOnThirdWeek.add(HistoricalStatus.Skipped) }
        repeat(3) { expectedStatusesOnThirdWeek.add(HistoricalStatus.NotCompleted) }
        repeat(2) { expectedStatusesOnThirdWeek.add(PlanningStatus.Planned) }

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = thirdWeekPeriod,
                today = LocalDate(2023, Month.OCTOBER, 28),
            )
        ).isEqualTo(
            expectedStatusesOnThirdWeek.mapIndexed { index, status ->
                StatusEntry(thirdWeekPeriod.first().plusDays(index), status)
            }
        )
    }

    @Test
    fun `WeeklyScheduleByNumOfDueDays, test backlog`() = runTest {
        val habit = Habit.YesNoHabit(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        habitRepository.insertHabit(habit)


        val forthWeekPeriod =
            LocalDate(2023, Month.OCTOBER, 30)..LocalDate(2023, Month.NOVEMBER, 5)

        val expectedStatusesOnForthWeek = mutableListOf<PlanningStatus>()
        repeat(5) { expectedStatusesOnForthWeek.add(PlanningStatus.Planned) }
        repeat(2) { expectedStatusesOnForthWeek.add(PlanningStatus.Backlog) }

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = forthWeekPeriod,
                today = LocalDate(2023, Month.OCTOBER, 28),
            )
        ).isEqualTo(
            expectedStatusesOnForthWeek.mapIndexed { index, status ->
                StatusEntry(forthWeekPeriod.first().plusDays(index), status)
            }
        )
    }

    @Test
    fun `WeeklyScheduleByNumOfDueDays, test cancelDuenessIfDoneAhead`() = runTest {
        val schedule = Schedule.WeeklyScheduleByNumOfDueDays(
            numOfDueDays = 4,
            numOfDueDaysInFirstPeriod = null,
            backlogEnabled = true,
            completingAheadEnabled = true,
            startDate = LocalDate(2023, Month.NOVEMBER, 6),
        )
        val habit = Habit.YesNoHabit(
            id = routineId,
            name = "",
            schedule = schedule,
        )
        habitRepository.insertHabit(habit)

        val completedDays =
            LocalDate(2023, Month.NOVEMBER, 6)..LocalDate(2023, Month.NOVEMBER, 9)
        completedDays.forEachIndexed { _, date ->
            routineCompletionHistoryRepository.insertHistoryEntry(
                routineId = routineId,
                entry = CompletionHistoryEntry(
                    date = date,
                    status = HistoricalStatus.Completed,
                    scheduleDeviation = 0F,
                    timesCompleted = 1F,
                )
            )
        }

        val overCompletedDays =
            LocalDate(2023, Month.NOVEMBER, 10)..LocalDate(2023, Month.NOVEMBER, 12)
        overCompletedDays.forEachIndexed { _, date ->
            routineCompletionHistoryRepository.insertHistoryEntry(
                routineId = routineId,
                entry = CompletionHistoryEntry(
                    date = date,
                    status = HistoricalStatus.OverCompleted,
                    scheduleDeviation = 1F,
                    timesCompleted = 1F,
                )
            )
        }

        val expectedStatusesOnSixthsWeek = mutableListOf<RoutineStatus>()
        repeat(3) { expectedStatusesOnSixthsWeek.add(PlanningStatus.AlreadyCompleted) }
        expectedStatusesOnSixthsWeek.add(PlanningStatus.Planned)
        repeat(3) { expectedStatusesOnSixthsWeek.add(PlanningStatus.NotDue) }

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = LocalDate(2023, Month.NOVEMBER, 13)..LocalDate(2023, Month.NOVEMBER, 19),
                today = LocalDate(2023, Month.NOVEMBER, 13),
            )
        ).isEqualTo(
            expectedStatusesOnSixthsWeek.mapIndexed { index, status ->
                StatusEntry(LocalDate(2023, Month.NOVEMBER, 13).plusDays(index), status)
            }
        )
    }

    @Test
    fun `get routine status after end date test`() = runTest {
        val habit = Habit.YesNoHabit(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        habitRepository.insertHabit(habit)

        val weekAfterEndDatePeriod =
            LocalDate(2023, Month.NOVEMBER, 13)..LocalDate(2023, Month.NOVEMBER, 19)

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = weekAfterEndDatePeriod,
                today = LocalDate(2023, Month.NOVEMBER, 19),
            )
        ).isEqualTo(emptyList<StatusEntry>())

        val randomDateBeforeRoutineStart =
            routineStartDate.minus(DatePeriod(days = Random.nextInt(1..50)))

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = randomDateBeforeRoutineStart..routineStartDate.minus(DatePeriod(days = 1)),
                today = LocalDate(2023, Month.NOVEMBER, 19),
            )
        ).isEqualTo(emptyList<StatusEntry>())
    }
}