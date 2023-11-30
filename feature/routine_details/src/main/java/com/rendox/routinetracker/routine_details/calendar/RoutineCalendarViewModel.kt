package com.rendox.routinetracker.routine_details.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.use_cases.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.ToggleHistoricalStatusUseCase
import com.rendox.routinetracker.core.domain.streak.GetDisplayStreaksUseCase
import com.rendox.routinetracker.core.domain.streak.checkIfContainDate
import com.rendox.routinetracker.core.domain.streak.getCurrentStreak
import com.rendox.routinetracker.core.domain.streak.getDurationInDays
import com.rendox.routinetracker.core.domain.streak.getLongestStreak
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.model.StatusEntry
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
    private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    private val routineId: Long,
    private val routineRepository: RoutineRepository,
    private val getRoutineStatusList: GetRoutineStatusUseCase,
    private val insertRoutineStatus: InsertRoutineStatusUseCase,
    private val toggleRoutineStatus: ToggleHistoricalStatusUseCase,
    private val getAllStreaks: GetDisplayStreaksUseCase
) : ViewModel() {
    private lateinit var routine: Routine

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
            routine = routineRepository.getRoutineById(routineId)
            streaksFlow.update {
                val streaks = getAllStreaks(routineId = routineId, today = today)
                println("all streaks = $streaks")
                streaks
            }
        }
        viewModelScope.launch {
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
        val routineStatusesForCurrentMonth: List<StatusEntry> = getRoutineStatusList(
            routineId = routineId,
            dates = period,
            today = today,
        )
        return routineStatusesForCurrentMonth.map {
            val includedInStreak = streaksFlow.value.checkIfContainDate(it.date)
            println("${it.date} included in streak = $includedInStreak")
            RoutineCalendarDate(
                date = it.date,
                status = it.status,
                includedInStreak = includedInStreak,
            )
        }
    }

    fun onScrolledToNewMonth(newMonth: YearMonth) {
        println("newMonth = $newMonth")
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

    fun onCalendarDateClick(date: LocalDate, routineStatusBeforeClick: RoutineStatus) {
        viewModelScope.launch {
            if (routineStatusBeforeClick is PlanningStatus) {
                insertRoutineStatus(
                    routineId = routineId,
                    currentDate = date,
                    completedOnCurrentDate = true,
                    today = today,
                )
            } else {
                toggleRoutineStatus(
                    routineId = routineId,
                    currentDate = date,
                    today = today,
                )
            }

            streaksFlow.update {
                getAllStreaks(routineId = routineId, today = today)
            }

            val monthStart = _currentMonthFlow.value.atStartOfMonth().toKotlinLocalDate()
            val monthEnd = _currentMonthFlow.value.atEndOfMonth().toKotlinLocalDate()
            val initialMonthPeriod = fetchAndDeriveCalendarDates(period = monthStart..monthEnd)
            cashedDatesFlow.update {
                it.toMutableMap().apply { put(_currentMonthFlow.value, initialMonthPeriod) }
            }
        }
    }
}

data class RoutineCalendarDate(
    val date: LocalDate,
    val status: RoutineStatus,
    val includedInStreak: Boolean,
)