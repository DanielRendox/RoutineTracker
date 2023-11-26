package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.use_cases.ToggleHistoricalStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.use_cases.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_time.GetRoutineCompletionTimeUseCase
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
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
    private val routineRepository: RoutineRepository,
    private val getRoutineStatus: GetRoutineStatusUseCase,
    private val insertRoutineStatus: InsertRoutineStatusUseCase,
    private val toggleHistoricalStatus: ToggleHistoricalStatusUseCase,
    private val getRoutineCompletionTime: GetRoutineCompletionTimeUseCase,
) : ViewModel() {

    private val todayFlow = MutableStateFlow(today)
    private val allRoutinesFlow = MutableStateFlow(emptyList<Routine>())
    private val cashedRoutinesFlow = MutableStateFlow(emptyMap<LocalDate, List<DisplayRoutine>>())
    private val _hideNotDueRoutinesFlow = MutableStateFlow(false)

    private val _currentDateFlow = MutableStateFlow(today)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val visibleRoutinesFlow: StateFlow<List<DisplayRoutine>> =
        combine(
            _currentDateFlow,
            cashedRoutinesFlow,
            _hideNotDueRoutinesFlow
        ) { currentDate, cashedRoutines, hideNotDueRoutines ->
            val currentDateRoutines = cashedRoutines[currentDate] ?: emptyList()
            if (hideNotDueRoutines) currentDateRoutines.filter { it.status in dueRoutineStatuses }
            else currentDateRoutines
        }.stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
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
            allRoutinesFlow.update { routineRepository.getAllRoutines() }

            val currentDate = _currentDateFlow.value
            val currentDateRoutines = getRoutinesForDate(currentDate)
            cashedRoutinesFlow.update {
                it.toMutableMap().apply {
                    put(currentDate, currentDateRoutines)
                }
            }
        }
    }

    private suspend fun getRoutinesForDate(date: LocalDate): List<DisplayRoutine> {
        val routines = mutableListOf<DisplayRoutine>()
        for (routine in allRoutinesFlow.value) {
            val routineStatus: RoutineStatus? = getRoutineStatus(
                routineId = routine.id!!,
                date = date,
                today = todayFlow.value,
            )
            val newRoutine: DisplayRoutine? = routineStatus?.let {
                DisplayRoutine(
                    name = routine.name,
                    id = routine.id!!,
                    status = it,
                    completionTime = getRoutineCompletionTime(
                        routineId = routine.id!!,
                        date = date,
                    )
                )
            }
            newRoutine?.let { routines.add(it) }
        }
        return routines
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

            val currentDateRoutines = getRoutinesForDate(_currentDateFlow.value)
            cashedRoutinesFlow.update {
                mapOf(_currentDateFlow.value to currentDateRoutines)
            }
        }
    }

    fun onDateChange(newDate: LocalDate) = viewModelScope.launch {
        cashedRoutinesFlow.update {
            it.toMutableMap().apply {
                put(newDate, getRoutinesForDate(newDate))
            }
        }
        _currentDateFlow.update { newDate }
    }

    fun onNotDueRoutinesVisibilityToggle() {
        _hideNotDueRoutinesFlow.update { !it }
    }

    fun onAddRoutineClick() {
        viewModelScope.launch {
            routineRepository.insertRoutine(routineList.first())
            allRoutinesFlow.update { routineRepository.getAllRoutines() }
            val currentDateRoutines = getRoutinesForDate(_currentDateFlow.value)
            cashedRoutinesFlow.update {
                mapOf(_currentDateFlow.value to currentDateRoutines)
            }
            println("current date = ${_currentDateFlow.value}")
            println("cashed routines flow = $cashedRoutinesFlow")
        }
    }
}

data class DisplayRoutine(
    val name: String,
    val id: Long,
    val status: RoutineStatus,
    val completionTime: LocalTime?,
)