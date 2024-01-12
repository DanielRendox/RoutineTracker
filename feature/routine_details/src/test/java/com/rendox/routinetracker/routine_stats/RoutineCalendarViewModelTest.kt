package com.rendox.routinetracker.routine_stats

import com.google.common.truth.Truth.assertThat
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.testcommon.fakes.habit.CompletionHistoryRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitData
import com.rendox.routinetracker.core.testcommon.fakes.habit.HabitRepositoryFake
import com.rendox.routinetracker.core.testcommon.fakes.habit.VacationRepositoryFake
import com.rendox.routinetracker.routine_details.calendar.CalendarDateData
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarViewModel
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarViewModel.Companion.LoadAheadThreshold
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarViewModel.Companion.NumOfMonthsToLoadAhead
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarViewModel.Companion.NumOfMonthsToLoadInitially
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin

class RoutineCalendarViewModelTest {

    private lateinit var habitRepository: HabitRepository
    private lateinit var completionHistoryRepository: CompletionHistoryRepository
    private lateinit var computeHabitStatus: HabitComputeStatusUseCase
    private lateinit var insertHabitCompletion: InsertHabitCompletionUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    val coroutineDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(coroutineDispatcher)

        val habitData = HabitData()
        habitRepository = HabitRepositoryFake(habitData)
        completionHistoryRepository = CompletionHistoryRepositoryFake(habitData)
        computeHabitStatus = HabitComputeStatusUseCase(
            habitRepository = habitRepository,
            vacationRepository = VacationRepositoryFake(habitData),
            completionHistoryRepository = completionHistoryRepository,
            defaultDispatcher = coroutineDispatcher,
        )
        insertHabitCompletion = InsertHabitCompletionUseCase(
            completionHistoryRepository = completionHistoryRepository,
            habitRepository = habitRepository,
        )

        habitRepository.insertHabit(
            Habit.YesNoHabit(
                id = 1L,
                name = "RoutineCalendarViewModelTest Habit",
                schedule = Schedule.EveryDaySchedule(
                    startDate = LocalDate(2022, 1, 1)
                ),
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `assert the first month is initialized with correct dates`() {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )
        val initialMonth = today.toJavaLocalDate().yearMonth
        val monthStart = initialMonth.atStartOfMonth().toKotlinLocalDate()
        val monthEnd = initialMonth.atEndOfMonth().toKotlinLocalDate()
        val resultingDates = viewModel.calendarDatesFlow.value.keys
        assertThat(resultingDates).containsAtLeastElementsIn(monthStart..monthEnd)
    }

    @Test
    fun `assert months before and after the initial month get preloaded`() {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )
        val initialMonth = today.toJavaLocalDate().yearMonth
        val pastPeriodStart = initialMonth.minusMonths(NumOfMonthsToLoadInitially.toLong())
            .atStartOfMonth().toKotlinLocalDate()
        val pastPeriodEnd = initialMonth.minusMonths(1)
            .atEndOfMonth().toKotlinLocalDate()
        val futurePeriodStart = initialMonth.plusMonths(1)
            .atStartOfMonth().toKotlinLocalDate()
        val futurePeriodEnd = initialMonth.plusMonths(NumOfMonthsToLoadInitially.toLong())
            .atEndOfMonth().toKotlinLocalDate()

        val resultingDates = viewModel.calendarDatesFlow.value.keys
        assertThat(resultingDates).containsAtLeastElementsIn(pastPeriodStart..pastPeriodEnd)
        assertThat(resultingDates).containsAtLeastElementsIn(futurePeriodStart..futurePeriodEnd)
    }

    @Test
    fun `assert already loaded months do not get loaded again upon scrolling`() {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )
        val initialDates = viewModel.calendarDatesFlow.value
        for (i in 1..(NumOfMonthsToLoadInitially - LoadAheadThreshold)) {
            viewModel.onScrolledToNewMonth(
                newMonth = today.toJavaLocalDate().yearMonth.plusMonths(i.toLong())
            )
        }
        assertThat(viewModel.calendarDatesFlow.value).containsExactlyEntriesIn(initialDates)

        for (i in 1..(NumOfMonthsToLoadInitially - LoadAheadThreshold)) {
            viewModel.onScrolledToNewMonth(
                newMonth = today.toJavaLocalDate().yearMonth.minusMonths(i.toLong())
            )
        }
        assertThat(viewModel.calendarDatesFlow.value).containsExactlyEntriesIn(initialDates)
    }

    @Test
    fun `assert exceeding the threshold in future results in additional future months loading`() {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )

        for (i in 1..(LoadAheadThreshold + 1)) {
            viewModel.onScrolledToNewMonth(
                newMonth = today.toJavaLocalDate().yearMonth.plusMonths(i.toLong())
            )
        }
        val resultingDates = viewModel.calendarDatesFlow.value.keys
        val firstExpectedDate =
            today.minus(DatePeriod(months = NumOfMonthsToLoadInitially))
        val lastExpectedDate =
            today.plus(DatePeriod(months = NumOfMonthsToLoadInitially + NumOfMonthsToLoadAhead))
                .atEndOfMonth
        assertThat(resultingDates).containsExactlyElementsIn(firstExpectedDate..lastExpectedDate)
    }

    @Test
    fun `assert exceeding the threshold in past results in additional past months loading`() {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )

        for (i in 1..(LoadAheadThreshold + 1)) {
            viewModel.onScrolledToNewMonth(
                newMonth = today.toJavaLocalDate().yearMonth.minusMonths(i.toLong())
            )
        }
        val resultingDates = viewModel.calendarDatesFlow.value.keys
        val firstExpectedDate = today.minus(
            DatePeriod(months = NumOfMonthsToLoadInitially + NumOfMonthsToLoadAhead)
        )
        val lastExpectedDate = today.plus(DatePeriod(months = NumOfMonthsToLoadInitially))
            .atEndOfMonth
        assertThat(resultingDates).containsExactlyElementsIn(firstExpectedDate..lastExpectedDate)
    }

    @Test
    fun `assert the data is updated when a completion is inserted`() = runTest {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )
        viewModel.onHabitComplete(
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = today,
                numOfTimesCompleted = 1F,
            )
        )
        val resultingDates = viewModel.calendarDatesFlow.value
        val expectedDates = (today..today.atEndOfMonth).associateWith {
            CalendarDateData(
                status = computeHabitStatus(habitId = 1L, validationDate = it, today = today),
                includedInStreak = false,
                numOfTimesCompleted = if (it == today) 1F else 0F,
            )
        }
        assertThat(resultingDates).containsAtLeastEntriesIn(expectedDates)
    }

    @Test
    fun `assert all other months get deleted when a completion is inserted`() = runTest {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )
        viewModel.onHabitComplete(
            completionRecord = Habit.YesNoHabit.CompletionRecord(
                date = today,
                numOfTimesCompleted = 1F,
            )
        )

        val resultingDates: Iterable<LocalDate> = viewModel.calendarDatesFlow.value.keys
        val expectedDates: Iterable<LocalDate> = today..today.atEndOfMonth
        assertThat(resultingDates).containsExactlyElementsIn(expectedDates)
    }

    @Test
    fun `assert months get loaded with the margin when scrolled to a not already loaded month`() = runTest {
        val today = LocalDate(2024, 1, 1)
        val viewModel = RoutineCalendarViewModel(
            routineId = 1L,
            today = today,
            habitRepository = habitRepository,
            computeHabitStatus = computeHabitStatus,
            completionHistoryRepository = completionHistoryRepository,
            insertHabitCompletion = insertHabitCompletion,
        )
        val futureMonth =
            today.toJavaLocalDate().yearMonth.plusMonths((NumOfMonthsToLoadInitially + 1).toLong())
        viewModel.onScrolledToNewMonth(futureMonth)

        // additional months get loaded only in the future because of the direction of the scroll
        val futureMonthStartDate = futureMonth.atStartOfMonth().toKotlinLocalDate()
        val lastDateToLoadAhead = futureMonth.plusMonths(NumOfMonthsToLoadAhead.toLong())
            .atEndOfMonth().toKotlinLocalDate()
        val expectedFutureDates: Iterable<LocalDate> = futureMonthStartDate..lastDateToLoadAhead

        val pastMonth =
            today.toJavaLocalDate().yearMonth.minusMonths((NumOfMonthsToLoadInitially + 1).toLong())
        viewModel.onScrolledToNewMonth(pastMonth)

        // additional months get loaded only in the past because of the direction of the scroll
        val pastMonthEndDate = pastMonth.atEndOfMonth().toKotlinLocalDate()
        val firstDateToLoadBehind = pastMonth.minusMonths(NumOfMonthsToLoadAhead.toLong())
            .atStartOfMonth().toKotlinLocalDate()
        val expectedPastDates: Iterable<LocalDate> = firstDateToLoadBehind..pastMonthEndDate

        val resultingDates: Iterable<LocalDate> = viewModel.calendarDatesFlow.value.keys
        assertThat(resultingDates).containsAtLeastElementsIn(expectedFutureDates)
        assertThat(resultingDates).containsAtLeastElementsIn(expectedPastDates)
    }
}