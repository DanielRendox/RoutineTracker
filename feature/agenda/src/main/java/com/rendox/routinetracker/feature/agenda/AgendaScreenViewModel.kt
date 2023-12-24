package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.data.routine.HabitRepository
import com.rendox.routinetracker.core.domain.completion_history.use_cases.ToggleHistoricalStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_time.GetRoutineCompletionTimeUseCase
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.RoutineStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class AgendaScreenViewModel(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    private val habitRepository: HabitRepository,
    private val getRoutineStatus: GetRoutineStatusUseCase,
    private val insertRoutineStatus: InsertRoutineStatusUseCase,
    private val toggleHistoricalStatus: ToggleHistoricalStatusUseCase,
    private val getRoutineCompletionTime: GetRoutineCompletionTimeUseCase,
) : ViewModel() {

    private val todayFlow = MutableStateFlow(today)
    private val allRoutinesFlow = MutableStateFlow(emptyList<Habit>())
    private val cashedRoutinesFlow = MutableStateFlow(emptyMap<LocalDate, List<DisplayRoutine>>())
    private val _hideNotDueRoutinesFlow = MutableStateFlow(false)

    private val _currentDateFlow = MutableStateFlow(today)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val visibleRoutinesFlow: StateFlow<List<DisplayRoutine>?> =
        combine(
            _currentDateFlow,
            cashedRoutinesFlow,
            _hideNotDueRoutinesFlow,
        ) { currentDate, cashedRoutines, hideNotDueRoutines ->
            cashedRoutines[currentDate]?.let { currentDateRoutines ->
                currentDateRoutines.filter {
                    if (hideNotDueRoutines) it.status in dueRoutineStatuses else true
                }.sortedBy { it.id }
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000),
        )

    private val dueRoutineStatuses = listOf<RoutineStatus>(
        PlanningStatus.Planned,
        PlanningStatus.Backlog,
        HistoricalStatus.NotCompleted,
        HistoricalStatus.Completed,
        HistoricalStatus.OverCompleted,
        HistoricalStatus.OverCompletedOnVacation,
        HistoricalStatus.SortedOutBacklogOnVacation,
        HistoricalStatus.SortedOutBacklog,
    )

    init {
        viewModelScope.launch {
            allRoutinesFlow.update { habitRepository.getAllHabits() }
            updateRoutinesForDate(_currentDateFlow.value)
        }
    }

    private fun updateRoutinesForDate(date: LocalDate) {
        for (routine in allRoutinesFlow.value) {
            viewModelScope.launch {
                val routineForDate = getDisplayRoutine(routine, date)
                cashedRoutinesFlow.update { cashedRoutines ->
                    cashedRoutines.toMutableMap().also {
                        val existingRoutines = it[date] ?: emptyList()
                        it[date] = existingRoutines.toMutableList().apply {
                            if (routineForDate != null) add(routineForDate)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getDisplayRoutine(habit: Habit, date: LocalDate): DisplayRoutine? {
        val routineStatus: RoutineStatus? = getRoutineStatus(
            routineId = habit.id!!,
            date = date,
            today = todayFlow.value,
        )
        return routineStatus?.let {
            DisplayRoutine(
                name = habit.name,
                id = habit.id!!,
                status = it,
                completionTime = null,
                hasGrayedOutLook = it !in dueRoutineStatuses,
                statusToggleIsDisabled = date > todayFlow.value,
            )
        }
    }

    fun onRoutineStatusCheckmarkClick(
        routineId: Long,
        currentDate: LocalDate,
        routineStatusBeforeClick: RoutineStatus,
    ) {
        viewModelScope.launch {
            if (routineStatusBeforeClick is PlanningStatus) {
                insertRoutineStatus(
                    routineId = routineId,
                    currentDate = currentDate,
                    completedOnCurrentDate = true,
                    today = todayFlow.value
                )
            } else {
                toggleHistoricalStatus(
                    routineId = routineId,
                    currentDate = currentDate,
                    today = todayFlow.value,
                )
            }

            cashedRoutinesFlow.update { cashedRoutines ->
                val newCashedRoutinesValue = cashedRoutines.toMutableMap()
                for ((date, oldRoutineList) in cashedRoutines) {
                    val routine = allRoutinesFlow.value.find { it.id == routineId }?.let {
                        getDisplayRoutine(
                            habit = it,
                            date = date,
                        )
                    }
                    newCashedRoutinesValue[date] = oldRoutineList.toMutableList().apply {
                        val routineToUpdateIndex = indexOf(find { it.id == routineId })
                        if (routine == null) removeAt(routineToUpdateIndex)
                        else set(routineToUpdateIndex, routine)
                    }
                }
                newCashedRoutinesValue
            }
        }
    }

    fun onDateChange(newDate: LocalDate) {
        _currentDateFlow.update { newDate }
        if (!cashedRoutinesFlow.value.contains(newDate)) {
            updateRoutinesForDate(newDate)
        }
    }

    fun onNotDueRoutinesVisibilityToggle() {
        _hideNotDueRoutinesFlow.update { !it }
    }

//    fun onAddRoutineClick() {
//        viewModelScope.launch {
//            routineRepository.insertRoutine(routineList.first())
//            allRoutinesFlow.update { routineRepository.getAllRoutines() }
//            val currentDateRoutines = getRoutinesForDate(_currentDateFlow.value)
//            cashedRoutinesFlow.update {
//                mapOf(_currentDateFlow.value to currentDateRoutines)
//            }
//            println("current date = ${_currentDateFlow.value}")
//            println("cashed routines flow = $cashedRoutinesFlow")
//        }
//    }
}

data class DisplayRoutine(
    val name: String,
    val id: Long,
    val status: RoutineStatus,
    val completionTime: LocalTime?,
    val hasGrayedOutLook: Boolean,
    val statusToggleIsDisabled: Boolean,
)