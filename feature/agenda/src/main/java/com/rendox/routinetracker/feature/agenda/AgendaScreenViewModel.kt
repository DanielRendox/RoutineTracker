package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.domain.agenda.GetAgendaUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.dueOrCompletedStatuses
import com.rendox.routinetracker.core.ui.helpers.UiEvent
import kotlinx.coroutines.Dispatchers
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
import kotlin.system.measureTimeMillis

class AgendaScreenViewModel(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    private val insertHabitCompletion: InsertHabitCompletionUseCase,
    private val getAgenda: GetAgendaUseCase,
) : ViewModel() {
    private val todayFlow = MutableStateFlow(today)
    private val agendaFlow: MutableStateFlow<List<DisplayRoutine>?> = MutableStateFlow(null)

    private val _showAllRoutinesFlow = MutableStateFlow(false)
    val showAllRoutinesFlow = _showAllRoutinesFlow.asStateFlow()

    private val _currentDateFlow = MutableStateFlow(today)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val visibleRoutinesFlow: StateFlow<List<DisplayRoutine>?> =
        combine(
            agendaFlow,
            _showAllRoutinesFlow,
        ) { cashedRoutines, showAllRoutines ->
            cashedRoutines?.filter {
                if (showAllRoutines) {
                    true
                } else {
                    it.status in dueOrCompletedStatuses
                }
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000),
        )

    private val _completionAttemptBlockedEvent: MutableStateFlow<UiEvent<IllegalDateEditAttemptException>?> =
        MutableStateFlow(null)
    val completionAttemptBlockedEvent = _completionAttemptBlockedEvent.asStateFlow()

    init {
        viewModelScope.launch {
            updateRoutinesForDate(_currentDateFlow.value)
        }
    }

    private suspend fun updateRoutinesForDate(date: LocalDate) {
        val duration = measureTimeMillis {
            val agenda = getAgenda(
                validationDate = date,
                today = todayFlow.value,
            )
            val routines = agenda.map { (habit, habitCompletionData) ->
                DisplayRoutine(
                    name = habit.name,
                    id = habit.id!!,
                    type = DisplayRoutineType.YesNoHabit,
                    status = habitCompletionData.habitStatus,
                    numOfTimesCompleted = habitCompletionData.numOfTimesCompleted,
                    completionTime = null,
                    hasGrayedOutLook = habitCompletionData.habitStatus !in dueOrCompletedStatuses,
                    statusToggleIsDisabled = date > todayFlow.value,
                )
            }
            agendaFlow.update { routines }
        }
        println("AgendaScreenViewModel updated routines for $date in $duration ms")
    }

    fun onRoutineComplete(
        routineId: Long,
        completionRecord: Habit.CompletionRecord,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                insertHabitCompletion(
                    habitId = routineId,
                    completionRecord = completionRecord,
                    today = todayFlow.value,
                )
            } catch (exception: IllegalDateEditAttemptException) {
                _completionAttemptBlockedEvent.update {
                    object : UiEvent<IllegalDateEditAttemptException> {
                        override val data: IllegalDateEditAttemptException = exception
                        override fun onConsumed() {
                            _completionAttemptBlockedEvent.update { null }
                        }
                    }
                }
                return@launch
            }

            updateRoutinesForDate(_currentDateFlow.value)
        }
    }

    fun onDateChange(newDate: LocalDate) {
        _currentDateFlow.update { newDate }
        agendaFlow.update { null }
        viewModelScope.launch {
            updateRoutinesForDate(_currentDateFlow.value)
        }
    }

    fun onNotDueRoutinesVisibilityToggle() {
        _showAllRoutinesFlow.update { !it }
    }
}

data class DisplayRoutine(
    val name: String,
    val id: Long,
    val type: DisplayRoutineType,
    val status: HabitStatus,
    val numOfTimesCompleted: Float,
    val completionTime: LocalTime?,
    val hasGrayedOutLook: Boolean,
    val statusToggleIsDisabled: Boolean,
)

enum class DisplayRoutineType {
    YesNoHabit,
}
