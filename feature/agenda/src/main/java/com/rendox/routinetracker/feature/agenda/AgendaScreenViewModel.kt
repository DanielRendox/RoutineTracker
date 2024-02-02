package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.domain.completion_history.GetHabitCompletionDataUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.domain.di.GetAllHabitsUseCase
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.dueOrCompletedStatuses
import com.rendox.routinetracker.core.ui.helpers.UiEvent
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
    private val getAllHabits: GetAllHabitsUseCase,
    private val insertHabitCompletion: InsertHabitCompletionUseCase,
    private val getHabitCompletionData: GetHabitCompletionDataUseCase,
) : ViewModel() {
    private val todayFlow = MutableStateFlow(today)
    private val allRoutinesFlow = MutableStateFlow(emptyList<Habit>())
    private val cashedRoutinesFlow = MutableStateFlow(emptyList<DisplayRoutine>())

    private val _showAllRoutinesFlow = MutableStateFlow(false)
    val showAllRoutinesFlow = _showAllRoutinesFlow.asStateFlow()

    private val _currentDateFlow = MutableStateFlow(today)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    private val routinesAreUpdatingFlow = MutableStateFlow(true)

    val visibleRoutinesFlow: StateFlow<List<DisplayRoutine>> =
        combine(
            cashedRoutinesFlow,
            _showAllRoutinesFlow,
        ) { cashedRoutines, showAllRoutines ->
            cashedRoutines.filter {
                if (showAllRoutines) {
                    true
                } else {
                    it.status in dueOrCompletedStatuses
                }
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000),
        )

    val nothingIsScheduledFlow =
        combine(
            routinesAreUpdatingFlow,
            visibleRoutinesFlow,
        ) { routinesAreUpdating, visibleRoutinesFlow ->
            visibleRoutinesFlow.isEmpty() && !routinesAreUpdating
        }.stateIn(
            scope = viewModelScope,
            initialValue = false,
            started = SharingStarted.WhileSubscribed(5_000),
        )

    private val _completionAttemptBlockedEvent: MutableStateFlow<UiEvent<IllegalDateEditAttemptException>?> =
        MutableStateFlow(null)
    val completionAttemptBlockedEvent = _completionAttemptBlockedEvent.asStateFlow()

    init {
        viewModelScope.launch {
            allRoutinesFlow.update { getAllHabits() }
            updateRoutinesForDate(_currentDateFlow.value)
        }
    }

    private fun updateRoutinesForDate(date: LocalDate) = viewModelScope.launch {
        routinesAreUpdatingFlow.update { true }
        cashedRoutinesFlow.update { emptyList() }
        for (routine in allRoutinesFlow.value) {
            cashedRoutinesFlow.update { cashedRoutines ->
                cashedRoutines.toMutableList().apply {
                    add(getDisplayRoutine(habit = routine, date = date))
                }
            }
        }
        routinesAreUpdatingFlow.update { false }
    }

    private suspend fun getDisplayRoutine(habit: Habit, date: LocalDate): DisplayRoutine {
        val habitCompletionData = getHabitCompletionData(
            validationDate = date,
            today = todayFlow.value,
            habitId = habit.id!!,
        )

        return DisplayRoutine(
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

    fun onRoutineComplete(
        routineId: Long,
        completionRecord: Habit.CompletionRecord,
    ) {
        viewModelScope.launch {
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

            val routine = allRoutinesFlow.value.find { it.id == routineId }?.let {
                getDisplayRoutine(
                    habit = it,
                    date = completionRecord.date,
                )
            }

            routinesAreUpdatingFlow.update { true }
            cashedRoutinesFlow.update { cashedRoutines ->
                cashedRoutines.toMutableList().apply {
                    val routineToUpdateIndex = indexOfFirst { it.id == routineId }
                    when {
                        routine == null && routineToUpdateIndex != -1 -> removeAt(
                            routineToUpdateIndex
                        )

                        routine != null && routineToUpdateIndex == -1 -> add(routine)
                        routine != null && routineToUpdateIndex != -1 -> set(
                            routineToUpdateIndex, routine,
                        )
                    }
                }
            }
            routinesAreUpdatingFlow.update { false }
        }
    }

    fun onDateChange(newDate: LocalDate) {
        _currentDateFlow.update { newDate }
        updateRoutinesForDate(newDate)
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
