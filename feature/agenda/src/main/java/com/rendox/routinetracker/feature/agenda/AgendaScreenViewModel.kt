package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.ToggleRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.routine.GetRoutineCompletionTimeUseCase
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
    private val routineRepository: RoutineRepository,
    private val getRoutineStatus: GetRoutineStatusUseCase,
    private val insertRoutineStatus: InsertRoutineStatusUseCase,
    private val toggleRoutineStatus: ToggleRoutineStatusUseCase,
    private val getRoutineCompletionTime: GetRoutineCompletionTimeUseCase,
) : ViewModel() {

    private val allRoutinesFlow = MutableStateFlow(emptyList<Routine>())
    private val cashedRoutinesFlow =
        MutableStateFlow(emptyMap<LocalDate, List<RoutineListElement>>())

    private val _hideNotDueRoutinesFlow = MutableStateFlow(false)
//    val hideNotDueRoutinesFlow = _hideNotDueRoutinesFlow.asStateFlow()

    private val _currentDateFlow =
        MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val visibleRoutinesFlow: StateFlow<List<RoutineListElement>> =
        cashedRoutinesFlow.combine(_currentDateFlow) { cashedRoutinesMap, currentDate ->
            println("AgendaScreenViewModel combine cashedRoutinesMap and currentDate: cashedRoutines = $cashedRoutinesMap, currentDate = $currentDate")
            if (cashedRoutinesMap.contains(currentDate)) {
                cashedRoutinesMap[currentDate]!!
            } else {
                val newValue = getRoutineElementsListForDate(currentDate, allRoutinesFlow.value)
                cashedRoutinesFlow.update {
                    it.toMutableMap().apply {
                        put(currentDate, newValue)
                    }
                }
                newValue
            }
        }.combine(_hideNotDueRoutinesFlow) { routinesForCurrentDate, hideNotDueRoutines ->
            println("AgendaScreenViewModel hide not due routines: routinesForCurrentDate = $routinesForCurrentDate, hideNotDueRoutines = $hideNotDueRoutines")
            if (hideNotDueRoutines) {
                routinesForCurrentDate.filter { it.status in dueRoutineStatuses }
            } else routinesForCurrentDate
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
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
            allRoutinesFlow.collect { allRoutines ->
                println("AgendaScreenViewModel collect allRoutinesFlow (allRoutines = $allRoutines)")
                cashedRoutinesFlow.update { cashedRoutinesMap ->
                    if (cashedRoutinesMap.isEmpty()) {
                        val currentDate: LocalDate = _currentDateFlow.value
                        val routinesForCurrentDate: List<RoutineListElement> =
                            getRoutineElementsListForDate(currentDate, allRoutines)
                        mapOf(currentDate to routinesForCurrentDate)
                    } else {
                        cashedRoutinesMap.toMutableMap().also {
                            for (currentDate in it.keys) {
                                it[currentDate] =
                                    getRoutineElementsListForDate(currentDate, allRoutines)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun getRoutineElementsListForDate(
        date: LocalDate, allRoutines: List<Routine>
    ): List<RoutineListElement> {
        val cashedRoutines = mutableListOf<RoutineListElement>()
        for (routine in allRoutines) {
            val newElementStatus: RoutineStatus? = getRoutineStatus(
                routineId = routine.id!!,
                date = date,
                today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            )
            val newElement: RoutineListElement? = newElementStatus?.let {
                RoutineListElement(
                    name = routine.name,
                    id = routine.id!!,
                    status = it,
                    completionTime = getRoutineCompletionTime(
                        routineId = routine.id!!,
                        date = date,
                    )
                )
            }
            newElement?.let { cashedRoutines.add(it) }
        }
        return cashedRoutines
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
                )
            } else {
                toggleRoutineStatus(
                    routineId = routineId,
                    date = currentDate,
                    today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                )
            }

            println("AgendaScreenViewModel: routineListElements before update: ${cashedRoutinesFlow.value}")

            cashedRoutinesFlow.update {
                val newRoutineListElements = it.toMutableMap()
                for ((date, routineElementsList) in it) {
                    val newRoutineElementsList = routineElementsList.toMutableList()
                    val elementIndex = routineElementsList.indexOfFirst { element ->
                        element.id == routineId
                    }
                    val newStatus: RoutineStatus? = getRoutineStatus(
                        routineId = routineId,
                        date = date,
                        today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    )
                    if (newStatus == null) {
                        newRoutineElementsList.removeAt(elementIndex)
                    } else {
                        val oldValue = routineElementsList[elementIndex]
                        newRoutineElementsList[elementIndex] = oldValue.copy(status = newStatus)
                    }
                    newRoutineListElements[date] = newRoutineElementsList
                }
                newRoutineListElements
            }

            println("AgendaScreenViewModel: routineListElements after update: ${cashedRoutinesFlow.value}")
        }
    }

    fun onDateChange(newDate: LocalDate) {
        _currentDateFlow.update { newDate }
    }

    fun onNotDueRoutinesVisibilityToggle() {
        _hideNotDueRoutinesFlow.update { !it }
    }

    fun onAddRoutineClick() {
        viewModelScope.launch {
            routineRepository.insertRoutine(routineList.first())
            allRoutinesFlow.update { routineRepository.getAllRoutines() }
        }
    }
}

data class RoutineListElement(
    val name: String,
    val id: Long,
    val status: RoutineStatus,
    val completionTime: LocalTime?,
)