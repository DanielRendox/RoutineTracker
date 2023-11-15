package com.rendox.routinetracker.routine_details.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.GetListOfStreaksUseCase
import com.rendox.routinetracker.core.domain.completion_history.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.ToggleRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.deriveDatesIncludedInStreak
import com.rendox.routinetracker.core.domain.completion_history.getCurrentStreakDurationInDays
import com.rendox.routinetracker.core.domain.completion_history.getLongestStreakDurationInDays
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val routineId: Long,
    private val routineRepository: RoutineRepository,
    private val getRoutineStatusList: GetRoutineStatusUseCase,
    private val getListOfStreaks: GetListOfStreaksUseCase,
    private val insertRoutineStatus: InsertRoutineStatusUseCase,
    private val toggleRoutineStatus: ToggleRoutineStatusUseCase,
) : ViewModel() {

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private lateinit var routine: Routine
    private val _currentMonthFlow: MutableStateFlow<YearMonth> =
        MutableStateFlow(today.toJavaLocalDate().yearMonth)
    val currentMonthFlow: StateFlow<YearMonth> = _currentMonthFlow.asStateFlow()

    private val cashedDatesFlow: MutableStateFlow<Map<YearMonth, List<RoutineCalendarDate>>> =
        MutableStateFlow(emptyMap())

    private val _visibleDatesFlow: MutableStateFlow<List<RoutineCalendarDate>> =
        MutableStateFlow(emptyList())
    val visibleDatesFlow: StateFlow<List<RoutineCalendarDate>> = _visibleDatesFlow.asStateFlow()

    private val _currentStreakDurationInDays: MutableStateFlow<Int?> = MutableStateFlow(null)
    val currentStreakDurationInDays: StateFlow<Int?> = _currentStreakDurationInDays.asStateFlow()

    private val _longestStreakDurationInDays: MutableStateFlow<Int?> = MutableStateFlow(null)
    val longestStreakDurationInDays: StateFlow<Int?> = _longestStreakDurationInDays.asStateFlow()

    init {
        println("View model initialized")
        viewModelScope.launch {
            println("RoutineCalendarViewModel 1")
            val monthStart = _currentMonthFlow.value.atStartOfMonth().toKotlinLocalDate()
            println("RoutineCalendarViewModel 2")
            val monthEnd = _currentMonthFlow.value.atEndOfMonth().toKotlinLocalDate()
            println("RoutineCalendarViewModel 3")
            val streaks = getListOfStreaks(routineId)
            println("RoutineCalendarViewModel 4")
            val initialCalendarDates = fetchAndDeriveCalendarDates(
                period = monthStart..monthEnd,
                streaksProvider = { streaks },
            )

            println("RoutineCalendarViewModel cashed dates flow updated")
            cashedDatesFlow.update {
                it.toMutableMap().apply { put(_currentMonthFlow.value, initialCalendarDates) }
            }

            println("RoutineCalendarViewModel visible dates flow updated")
            _visibleDatesFlow.update {
                cashedDatesFlow.value[_currentMonthFlow.value]!!
            }

            routine = routineRepository.getRoutineById(routineId)
            val currentStreakDuration = getCurrentStreakDurationInDays(
                lastStreak = streaks.lastOrNull(),
                today = today,
                schedule = routine.schedule,
            )
            _currentStreakDurationInDays.update { currentStreakDuration }
            val longestStreakDuration = getLongestStreakDurationInDays(
                allStreaks = streaks,
                today = today,
                schedule = routine.schedule,
            )
            _longestStreakDurationInDays.update { longestStreakDuration }
        }
    }

    fun onCalendarDateClick(date: LocalDate, routineStatusBeforeClick: RoutineStatus) {
        viewModelScope.launch {
            if (routineStatusBeforeClick is PlanningStatus) {
                insertRoutineStatus(
                    routineId = routineId,
                    currentDate = date,
                    completedOnCurrentDate = true,
                )
            } else {
                toggleRoutineStatus(
                    routineId = routineId,
                    date = date,
                    today = today,
                )
            }

            val newStreaks = getListOfStreaks(routineId)

            cashedDatesFlow.update { cashedDates ->
                val newValue = cashedDates.toMutableMap()
                for (month in cashedDates.keys) {
                    if (cashedDates[month]!!.isNotEmpty()) {
                        val monthStart = cashedDates[month]!!.first().date
                        val monthEnd = cashedDates[month]!!.last().date
                        newValue[month] =
                            fetchAndDeriveCalendarDates(monthStart..monthEnd) { newStreaks }
                    }
                }
                newValue
            }

            _visibleDatesFlow.update {
                cashedDatesFlow.value[_currentMonthFlow.value]!!
            }

            val currentStreakDuration = getCurrentStreakDurationInDays(
                lastStreak = newStreaks.lastOrNull(),
                today = today,
                schedule = routine.schedule,
            )
            _currentStreakDurationInDays.update { currentStreakDuration }
            val longestStreakDuration = getLongestStreakDurationInDays(
                allStreaks = newStreaks,
                today = today,
                schedule = routine.schedule,
            )
            _longestStreakDurationInDays.update { longestStreakDuration }
        }
    }

    fun onScrolledToNewMonth(month: YearMonth) {
        _currentMonthFlow.update { month }

        viewModelScope.launch {
            if (!cashedDatesFlow.value.contains(month)) {
                val monthStart = month.atStartOfMonth().toKotlinLocalDate()
                val monthEnd = month.atEndOfMonth().toKotlinLocalDate()
                val currentMonthDates = fetchAndDeriveCalendarDates(monthStart..monthEnd)
                cashedDatesFlow.update {
                    it.toMutableMap().apply { put(month, currentMonthDates) }
                }
            }

            _visibleDatesFlow.update {
                cashedDatesFlow.value[month]!!
            }
        }
    }

    private suspend fun fetchAndDeriveCalendarDates(
        period: LocalDateRange,
        streaksProvider: suspend () -> List<Streak> = { getListOfStreaks(routineId) }
    ): List<RoutineCalendarDate> {
        val routineStatusesForCurrentMonth: List<StatusEntry> = getRoutineStatusList(
            routineId = routineId,
            dates = period,
            today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        )
        val streaks: List<Streak> = streaksProvider()
        val streakDates = deriveDatesIncludedInStreak(streaks, period)
        return routineStatusesForCurrentMonth.map {
            RoutineCalendarDate(
                date = it.date,
                status = it.status,
                includedInStreak = streakDates.contains(it.date),
            )
        }
    }
}

data class RoutineCalendarDate(
    val date: LocalDate,
    val status: RoutineStatus,
    val includedInStreak: Boolean,
)