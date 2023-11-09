package com.rendox.routinetracker.routine_details.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.GetListOfStreaksUseCase
import com.rendox.routinetracker.core.domain.completion_history.GetRoutineStatusListUseCase
import com.rendox.routinetracker.core.domain.completion_history.deriveDatesIncludedInStreak
import com.rendox.routinetracker.core.domain.completion_history.getCurrentStreakDurationInDays
import com.rendox.routinetracker.core.domain.completion_history.getLongestStreakDurationInDays
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.logic.time.withDayOfMonth
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.ui.helpers.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn

class RoutineCalendarViewModel(
    private val routineId: Long,
    private val routineRepository: RoutineRepository,
    private val getRoutineStatusList: GetRoutineStatusListUseCase,
    private val getListOfStreaks: GetListOfStreaksUseCase,
) : ViewModel() {

    private val _routineCalendarState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val routineCalendarState: StateFlow<UiState> = _routineCalendarState.asStateFlow()

    init {
        viewModelScope.launch {
            val streaks: List<Streak> = getListOfStreaks(routineId)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val periodToFetchData = today.withDayOfMonth(1)..(today.atEndOfMonth)
            val streakDates = deriveDatesIncludedInStreak(streaks, periodToFetchData)
            val routine = routineRepository.getRoutineById(routineId)
            val routineStatuses = getRoutineStatusList(routineId, periodToFetchData, today)

            _routineCalendarState.update {
                UiState.Success(
                    CalendarScreenUiState(
                        currentMonth = today.toJavaLocalDate().yearMonth,
                        firstDayOfWeek = today.dayOfWeek,
                        routineStatuses = routineStatuses,
                        streakDates = streakDates,
                        today = today,
                        currentStreakDurationInDays = getCurrentStreakDurationInDays(
                            lastStreak = streaks.lastOrNull(),
                            today = today,
                            schedule = routine.schedule,
                        ),
                        longestStreakDurationInDays = getLongestStreakDurationInDays(
                            allStreaks = streaks,
                            today = today,
                            schedule = routine.schedule,
                        ),
                    )
                )
            }
        }
    }
}

data class CalendarScreenUiState(
    val currentMonth: java.time.YearMonth,
    val firstDayOfWeek: java.time.DayOfWeek,
    val routineStatuses: List<StatusEntry>,
    val streakDates: List<LocalDate>,
    val today: LocalDate,
    val currentStreakDurationInDays: Int?,
    val longestStreakDurationInDays: Int?,
)