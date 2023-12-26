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
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import java.time.YearMonth

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
        MutableStateFlow(today.toJavaLocalDate().yearMonth)
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

            val monthStart = _currentMonthFlow.value.atStartOfMonth().toKotlinLocalDate()
            val monthEnd = _currentMonthFlow.value.atEndOfMonth().toKotlinLocalDate()
            val initialMonthPeriod = fetchAndDeriveCalendarDates(period = monthStart..monthEnd)
            cashedDatesFlow.update {
                it.toMutableMap().apply { put(_currentMonthFlow.value, initialMonthPeriod) }
            }
        }
    }

    private suspend fun fetchAndDeriveCalendarDates(
        period: LocalDateRange
    ): List<RoutineCalendarDate> {
        val currentMonthCalendarDates = mutableListOf<RoutineCalendarDate>()
        for (date in period) {
            val habitStatus = computeHabitStatus(
                habitId = _habitFlow.value?.id!!,
                validationDate = date,
                today = todayFlow.value,
            )
            val numOfTimesCompleted = completionHistoryRepository.getRecordByDate(
                habitId = _habitFlow.value?.id!!,
                date = date,
            )?.numOfTimesCompleted ?: 0F

            currentMonthCalendarDates.add(
                RoutineCalendarDate(
                    date = date,
                    status = habitStatus,
                    includedInStreak = false, // TODO Implement streaks
                    numOfTimesCompleted = numOfTimesCompleted,
                )
            )
        }
        return currentMonthCalendarDates
    }

    fun onScrolledToNewMonth(newMonth: YearMonth) {
        _currentMonthFlow.update { newMonth }

        if (!cashedDatesFlow.value.contains(newMonth)) {
            viewModelScope.launch {
                val monthStart = newMonth.atStartOfMonth().toKotlinLocalDate()
                val monthEnd = newMonth.atEndOfMonth().toKotlinLocalDate()
                val currentMonthDates = fetchAndDeriveCalendarDates(monthStart..monthEnd)
                cashedDatesFlow.update {
                    it.toMutableMap().apply { put(newMonth, currentMonthDates) }
                }
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

            val monthStart = _currentMonthFlow.value.atStartOfMonth().toKotlinLocalDate()
            val monthEnd = _currentMonthFlow.value.atEndOfMonth().toKotlinLocalDate()
            val initialMonthPeriod = fetchAndDeriveCalendarDates(period = monthStart..monthEnd)
            cashedDatesFlow.update {
                it.toMutableMap().apply { put(_currentMonthFlow.value, initialMonthPeriod) }
            }
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