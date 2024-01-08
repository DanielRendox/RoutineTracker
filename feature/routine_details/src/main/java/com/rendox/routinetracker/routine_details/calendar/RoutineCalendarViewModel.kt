package com.rendox.routinetracker.routine_details.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.streak.getCurrentStreak
import com.rendox.routinetracker.core.domain.streak.getDurationInDays
import com.rendox.routinetracker.core.domain.streak.getLongestStreak
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class RoutineCalendarViewModel(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    private val routineId: Long,
    private val habitRepository: HabitRepository,
    private val computeHabitStatus: HabitComputeStatusUseCase,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val insertHabitCompletion: InsertHabitCompletionUseCase,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    private val _habitFlow: MutableStateFlow<Habit?> = MutableStateFlow(null)
    val habitFlow: StateFlow<Habit?> = _habitFlow.asStateFlow()

    private val todayFlow = MutableStateFlow(today)

    private val _calendarDatesFlow = MutableStateFlow(emptyMap<LocalDate, CalendarDateData>())
    val calendarDatesFlow = _calendarDatesFlow.asStateFlow()

    private val streaksFlow = MutableStateFlow(emptyList<DisplayStreak>())

    private val _currentMonthFlow: MutableStateFlow<YearMonth> =
        MutableStateFlow(YearMonth.from(todayFlow.value.toJavaLocalDate()))
    val currentMonthFlow: StateFlow<YearMonth> = _currentMonthFlow.asStateFlow()

    val currentStreakDurationInDays: StateFlow<Int> = streaksFlow.map { streaks ->
        streaks.getCurrentStreak(today)?.getDurationInDays() ?: 0
    }.stateIn(
        scope = viewModelScope,
        initialValue = 0,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    val longestStreakDurationInDays: StateFlow<Int> = streaksFlow.map { streaks ->
        streaks.getLongestStreak()?.getDurationInDays() ?: 0
    }.stateIn(
        scope = viewModelScope,
        initialValue = 0,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    private val updateMonthJobsRunningFlow = MutableStateFlow(emptyMap<YearMonth, List<Job>>())

    init {
        // TODO remove explicit main dispatcher
        viewModelScope.launch(mainDispatcher) {
            _habitFlow.update { habitRepository.getHabitById(routineId) }
        }

        viewModelScope.launch(defaultDispatcher) {
            updateMonth(_currentMonthFlow.value)
            for (i in 1..NumOfMonthsToLoadInitially) {
                updateMonth(_currentMonthFlow.value.plusMonths(i.toLong()))
                updateMonth(_currentMonthFlow.value.minusMonths(i.toLong()))
            }
        }
    }

    /**
     * @param forceUpdate update the data even if it's already loaded
     */
    private suspend fun updateMonth(month: YearMonth, forceUpdate: Boolean = false) {
        if (!forceUpdate) {
            val dataForMonthIsAlreadyLoaded = _calendarDatesFlow.value.keys.find {
                it.toJavaLocalDate().yearMonth == month
            } != null
            if (dataForMonthIsAlreadyLoaded) return
        }

        val thisMonthIsAlreadyBeingUpdated =
            updateMonthJobsRunningFlow.value.contains(month)
        if (thisMonthIsAlreadyBeingUpdated) {
            if (forceUpdate) {
                updateMonthJobsRunningFlow.value[month]?.forEach { it.cancel() }
            } else {
                return
            }
        }

        val monthStart = month.atStartOfMonth().toKotlinLocalDate()
        val monthEnd = month.atEndOfMonth().toKotlinLocalDate()

        for (date in monthStart..monthEnd) {
            val job = viewModelScope.launch(defaultDispatcher) {
                updateStatusForDate(date)
            }
            updateMonthJobsRunningFlow.update { updateMonthJobsRunning ->
                updateMonthJobsRunning.toMutableMap().also {
                    it[month] = updateMonthJobsRunning[month].orEmpty().toMutableList().apply {
                        add(job)
                    }
                }
            }
        }

        updateMonthJobsRunningFlow.value[month]?.joinAll()
        updateMonthJobsRunningFlow.update { oldValue ->
            oldValue.toMutableMap().apply { remove(month) }
        }
    }

    private suspend fun updateStatusForDate(date: LocalDate) {
        val habitStatus = computeHabitStatus(
            habitId = routineId,
            validationDate = date,
            today = todayFlow.value,
        )
        val numOfTimesCompleted = completionHistoryRepository.getRecordByDate(
            habitId = routineId,
            date = date,
        )?.numOfTimesCompleted ?: 0F

        val calendarDateData = CalendarDateData(
            status = habitStatus,
            includedInStreak = false, // TODO Implement streaks
            numOfTimesCompleted = numOfTimesCompleted,
        )

        _calendarDatesFlow.update { oldValue ->
            oldValue.toMutableMap().also {
                it[date] = calendarDateData
            }
        }
    }

    fun onScrolledToNewMonth(newMonth: YearMonth) {
        _currentMonthFlow.update { newMonth }
        viewModelScope.launch(mainDispatcher) {
            updateMonth(month = newMonth)

            val latestDate = _calendarDatesFlow.value.keys.maxOrNull()?.toJavaLocalDate()
            val latestMonth = latestDate?.let { YearMonth.from(it) }
            if (latestMonth != null) {
                val numOfMonthsUntilLastLoadedMonth =
                    newMonth.until(latestMonth, ChronoUnit.MONTHS)
                if (numOfMonthsUntilLastLoadedMonth < LoadAheadThreshold) {
                    for (monthNumber in 1..NumOfMonthsToLoadAhead) {
                        updateMonth(latestMonth.plusMonths(monthNumber.toLong()))
                    }
                }
            }

            val earliestDate = _calendarDatesFlow.value.keys.minOrNull()?.toJavaLocalDate()
            val earliestMonth = earliestDate?.let { YearMonth.from(it) }
            if (earliestMonth != null) {
                val numOfMonthsUntilFirstLoadedMonth =
                    earliestMonth.until(newMonth, ChronoUnit.MONTHS)
                if (numOfMonthsUntilFirstLoadedMonth < LoadAheadThreshold) {
                    for (monthNumber in 1..NumOfMonthsToLoadAhead) {
                        updateMonth(earliestMonth.minusMonths(monthNumber.toLong()))
                    }
                }
            }
        }
    }

    fun onHabitComplete(completionRecord: Habit.CompletionRecord) {
        updateMonthJobsRunningFlow.value.keys.forEach { month ->
            updateMonthJobsRunningFlow.value[month]?.forEach { it.cancel() }
        }

        viewModelScope.launch(mainDispatcher) {
            try {
                insertHabitCompletion(
                    habitId = routineId,
                    completionRecord = completionRecord,
                    today = todayFlow.value,
                )
            } catch (e: InsertHabitCompletionUseCase.IllegalDateException) {
                // TODO display a snackbar
            }

            // update the completed day first to reflect the change in the UI as fast as possible
            withContext(defaultDispatcher) {
                updateStatusForDate(completionRecord.date)
            }

            updateMonth(_currentMonthFlow.value, forceUpdate = true)
            for (i in 1..NumOfMonthsToLoadInitially) {
                updateMonth(
                    month = _currentMonthFlow.value.plusMonths(i.toLong()),
                    forceUpdate = true,
                )
                updateMonth(
                    month = _currentMonthFlow.value.minusMonths(i.toLong()),
                    forceUpdate = true,
                )
            }
        }
    }

// TODO update todayFlow when the date changes (in case the screen is opened at midnight)

    companion object {
        /**
         * The number of months that should be loaded in both directions when the list of data is
         * empty. It's done for the user to see the pre-loaded data when they scroll to the next
         * month.
         */
        const val NumOfMonthsToLoadInitially = 6

        /**
         * The number of months that should be loaded ahead or behind the current month (depending
         * on the user's scroll direction). It's done for the user to see the pre-loaded data
         * when they scroll to the next month.
         */
        const val NumOfMonthsToLoadAhead = 3

        /**
         * The maximum number of months between the current month and the last/first loaded month
         * that doesn't trigger the pre-loading of future/past months. If this threshold is
         * exceeded, the data should be loaded. It's done for the user to see the pre-loaded data
         * when they scroll to the next month.
         */
        const val LoadAheadThreshold = 3
    }
}

data class CalendarDateData(
    val status: HabitStatus,
    val includedInStreak: Boolean,
    val numOfTimesCompleted: Float,
)