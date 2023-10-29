package com.rendox.routinetracker.core.domain.completion_history

import com.google.common.truth.Truth.assertThat
import com.rendox.routinetracker.core.data.di.completionHistoryDataModule
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.data.di.routineDataModule
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.routine.RoutineLocalDataSource
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.testcommon.fakes.routine.CompletionHistoryLocalDataSourceFake
import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineData
import com.rendox.routinetracker.core.testcommon.fakes.routine.RoutineLocalDataSourceFake
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

class GetRoutineStatusListUseCaseTest : KoinTest {

    private lateinit var insertRoutineStatusIntoHistory: InsertRoutineStatusIntoHistoryUseCase
    private lateinit var getRoutineStatusList: GetRoutineStatusListUseCase
    private lateinit var routineRepository: RoutineRepository

    private val routineId = 1L
    private val routineStartDate = LocalDate(2023, Month.OCTOBER, 11)
    private val routineEndDate = LocalDate(2023, Month.NOVEMBER, 12)

    private var weeklyScheduleByNumOfDueDays = Schedule.WeeklyScheduleByNumOfDueDays(
        numOfDueDays = 5,
        numOfDueDaysInFirstPeriod = 4,
        startDayOfWeek = DayOfWeek.MONDAY,
        backlogEnabled = true,
        cancelDuenessIfDoneAhead = true,
        periodSeparationEnabled = false,
        routineStartDate = routineStartDate,
        routineEndDate = routineEndDate,
    )


    private val testModule = module {
        single {
            RoutineData()
        }

        single<RoutineLocalDataSource> {
            RoutineLocalDataSourceFake(routineData = get())
        }

        single<CompletionHistoryLocalDataSource> {
            CompletionHistoryLocalDataSourceFake(routineData = get())
        }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                routineDataModule,
                completionHistoryDataModule,
                testModule,
            )
        }

        routineRepository = get()

        insertRoutineStatusIntoHistory = InsertRoutineStatusIntoHistoryUseCase(
            completionHistoryRepository = get(),
            routineRepository = get(),
        )

        getRoutineStatusList = GetRoutineStatusListUseCase(
            routineRepository = get(),
            completionHistoryRepository = get(),
            insertRoutineStatusIntoHistory = insertRoutineStatusIntoHistory,
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `get routine status for dates prior to routine start, returns empty list`() = runTest {
        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        routineRepository.insertRoutine(routine)

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
        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        routineRepository.insertRoutine(routine)

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
        assertThat(routineRepository.getRoutineById(routineId)).isEqualTo(routine)
    }

    @Test
    fun `WeeklyScheduleByNumOfDueDays, test history pre-population`() = runTest {
        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        routineRepository.insertRoutine(routine)

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
        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        routineRepository.insertRoutine(routine)


        val forthWeekPeriod =
            LocalDate(2023, Month.OCTOBER, 30)..LocalDate(2023, Month.NOVEMBER, 5)

        getRoutineStatusList(
            routineId = routineId,
            dates = forthWeekPeriod,
            today = LocalDate(2023, Month.OCTOBER, 28),
        ) // to prepopulate history with backlog

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
        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        routineRepository.insertRoutine(routine)

        val sixWeekPeriod =
            LocalDate(2023, Month.NOVEMBER, 6)..LocalDate(2023, Month.NOVEMBER, 12)

        getRoutineStatusList(
            routineId = routineId,
            dates = sixWeekPeriod,
            today = LocalDate(2023, Month.NOVEMBER, 6),
        ) // to populate the completion history

        routineRepository.updateScheduleDeviation(2, routineId)

        val expectedStatusesOnSixthsWeek = mutableListOf<RoutineStatus>()
        repeat(2) { expectedStatusesOnSixthsWeek.add(PlanningStatus.AlreadyCompleted) }
        repeat(3) { expectedStatusesOnSixthsWeek.add(PlanningStatus.Planned) }
        repeat(2) { expectedStatusesOnSixthsWeek.add(PlanningStatus.NotDue) }

        assertThat(
            getRoutineStatusList(
                routineId = routineId,
                dates = sixWeekPeriod,
                today = LocalDate(2023, Month.NOVEMBER, 6),
            )
        ).isEqualTo(
            expectedStatusesOnSixthsWeek.mapIndexed { index, status ->
                StatusEntry(sixWeekPeriod.first().plusDays(index), status)
            }
        )
    }

    @Test
    fun `get routine status after end date test`() = runTest {
        val routine = Routine.YesNoRoutine(
            id = routineId,
            name = "",
            schedule = weeklyScheduleByNumOfDueDays,
        )

        routineRepository.insertRoutine(routine)

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