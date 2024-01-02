package com.rendox.routinetracker.routine_details.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.atStartOfMonth
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
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
) : ViewModel() {
    private val _habitFlow: MutableStateFlow<Habit?> = MutableStateFlow(null)
    val habitFlow: StateFlow<Habit?> = _habitFlow.asStateFlow()

    private val todayFlow = MutableStateFlow(today)

    private val cashedDatesFlow: MutableStateFlow<Map<YearMonth, List<RoutineCalendarDate>>> =
        MutableStateFlow(emptyMap())
    private val streaksFlow = MutableStateFlow(emptyList<DisplayStreak>())

    private val _currentMonthFlow: MutableStateFlow<YearMonth> =
        MutableStateFlow(YearMonth.of(today.year, today.month))
    val currentMonthFlow: StateFlow<YearMonth> = _currentMonthFlow.asStateFlow()

    val visibleDatesFlow: StateFlow<List<RoutineCalendarDate>> =
        combine(_currentMonthFlow, cashedDatesFlow) { currentMonth, cashedDates ->
            cashedDates[currentMonth] ?: emptyList()
        }.stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000),
        )

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

    init {
        viewModelScope.launch {
            _habitFlow.update { habitRepository.getHabitById(routineId) }
        }

        viewModelScope.launch {
            val visibleMonthStart = _currentMonthFlow.value.atStartOfMonth().toKotlinLocalDate()
            val visibleMonthEnd = _currentMonthFlow.value.atEndOfMonth().toKotlinLocalDate()
            val updateVisibleDateJobs = mutableListOf<Job>()
            for (date in visibleMonthStart..visibleMonthEnd) {
                val job = launch {
                    updateStatusForDate(date)
                }
                updateVisibleDateJobs.add(job)
            }
            updateVisibleDateJobs.joinAll()

            updateNextMonths(currentMonth = _currentMonthFlow.value, numOfMonthsToUpdate = 5)
            updatePreviousMonths(currentMonth = _currentMonthFlow.value, numOfMonthsToUpdate = 5)
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

        val routineCalendarDate = RoutineCalendarDate(
            date = date,
            status = habitStatus,
            includedInStreak = false, // TODO Implement streaks
            numOfTimesCompleted = numOfTimesCompleted,
        )

        cashedDatesFlow.update { oldValue ->
            oldValue.toMutableMap().also { cashedDates ->
                val currentMonth = YearMonth.of(date.year, date.month)
                val currentMonthDates = cashedDates[currentMonth] ?: emptyList()
                cashedDates[currentMonth] = currentMonthDates.toMutableList().apply {
                    val indexOfDate = indexOfFirst { it.date == date }
                    if (indexOfDate == -1) {
                        add(routineCalendarDate)
                    } else {
                        set(indexOfDate, routineCalendarDate)
                    }
                }
            }
        }
    }

    private fun CoroutineScope.updatePreviousMonths(
        currentMonth: YearMonth, numOfMonthsToUpdate: Int
    ) {
        val pastPeriodStart =
            currentMonth.minusMonths(numOfMonthsToUpdate.toLong()).atStartOfMonth()
                .toKotlinLocalDate()
        val pastPeriodEnd =
            currentMonth.minusMonths(1).atEndOfMonth().toKotlinLocalDate()
        var date = pastPeriodEnd
        while (date != pastPeriodStart) {
            launch {
                updateStatusForDate(date)
            }
            date = date.minus(DatePeriod(days = 1))
        }
    }

    private fun CoroutineScope.updateNextMonths(
        currentMonth: YearMonth, numOfMonthsToUpdate: Int
    ) {
        val futurePeriodStart =
            currentMonth.plusMonths(1).atStartOfMonth().toKotlinLocalDate()
        val futurePeriodEnd =
            currentMonth.plusMonths(numOfMonthsToUpdate.toLong()).atEndOfMonth().toKotlinLocalDate()
        for (date in futurePeriodStart..futurePeriodEnd) {
            launch {
                updateStatusForDate(date)
            }
        }
    }

    fun onScrolledToNewMonth(newMonth: YearMonth) {
        val oldMonth = _currentMonthFlow.value
        _currentMonthFlow.update { newMonth }

        viewModelScope.launch {
            if (!cashedDatesFlow.value.contains(newMonth) && newMonth != oldMonth) {
                val visibleMonthStart = _currentMonthFlow.value.atStartOfMonth().toKotlinLocalDate()
                val visibleMonthEnd = _currentMonthFlow.value.atEndOfMonth().toKotlinLocalDate()
                val updateVisibleDateJobs = mutableListOf<Job>()
                for (date in visibleMonthStart..visibleMonthEnd) {
                    val job = launch {
                        updateStatusForDate(date)
                    }
                    updateVisibleDateJobs.add(job)
                }
                updateVisibleDateJobs.joinAll()
            }

            val latestMonth = cashedDatesFlow.value.keys.maxOrNull()
            if (latestMonth != null && newMonth.until(latestMonth, ChronoUnit.MONTHS) <= 3) {
                updateNextMonths(currentMonth = newMonth, numOfMonthsToUpdate = 3)
            }

            val earliestMonth = cashedDatesFlow.value.keys.minOrNull()
            if (earliestMonth != null && newMonth.until(earliestMonth, ChronoUnit.MONTHS) >= -3) {
                updatePreviousMonths(currentMonth = newMonth, numOfMonthsToUpdate = 3)
            }
        }
    }

    fun onHabitComplete(completionRecord: Habit.CompletionRecord) {
        viewModelScope.launch {
            try {
                insertHabitCompletion(
                    habitId = routineId,
                    completionRecord = completionRecord,
                    today = todayFlow.value,
                )
            } catch (e: InsertHabitCompletionUseCase.IllegalDateException) {
                // TODO display a snackbar
            }

            // update the completed date first for better UX
            updateStatusForDate(completionRecord.date)

            val visibleMonthStart = _currentMonthFlow.value.atStartOfMonth().toKotlinLocalDate()
            val visibleMonthEnd = _currentMonthFlow.value.atEndOfMonth().toKotlinLocalDate()
            val updateVisibleDateJobs = mutableListOf<Job>()
            for (date in visibleMonthStart..visibleMonthEnd) {
                val job = launch {
                    updateStatusForDate(date)
                }
                updateVisibleDateJobs.add(job)
            }
            updateVisibleDateJobs.joinAll()

            // remove the other months because the data can be outdated
            cashedDatesFlow.update { oldValue ->
                val currentMonthDates = oldValue[_currentMonthFlow.value] ?: emptyList()
                mapOf(_currentMonthFlow.value to currentMonthDates)
            }

            updateNextMonths(currentMonth = _currentMonthFlow.value, numOfMonthsToUpdate = 5)
            updatePreviousMonths(currentMonth = _currentMonthFlow.value, numOfMonthsToUpdate = 5)
        }
    }

    // TODO update todayFlow when the date changes (in case the screen is opened at midnight)
}

data class RoutineCalendarDate(
    val date: LocalDate,
    val status: HabitStatus,
    val includedInStreak: Boolean,
    val numOfTimesCompleted: Float,
)