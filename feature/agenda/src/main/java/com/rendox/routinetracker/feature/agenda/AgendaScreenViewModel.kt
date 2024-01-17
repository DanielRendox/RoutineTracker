package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.domain.completion_history.GetHabitCompletionDataUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.domain.di.GetAllHabitsUseCase
import com.rendox.routinetracker.core.domain.di.InsertHabitUseCase
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.dueOrCompletedStatuses
import com.rendox.routinetracker.core.model.nonExistentStatuses
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
    private val insertHabit: InsertHabitUseCase,
    private val insertHabitCompletion: InsertHabitCompletionUseCase,
    private val getHabitCompletionData: GetHabitCompletionDataUseCase,
) : ViewModel() {
    private val todayFlow = MutableStateFlow(today)
    private val allRoutinesFlow = MutableStateFlow(emptyList<Habit>())
    private val _hideNotDueRoutinesFlow = MutableStateFlow(false)
    private val cashedRoutinesFlow = MutableStateFlow(emptyList<DisplayRoutine>())

    private val _currentDateFlow = MutableStateFlow(today)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val visibleRoutinesFlow: StateFlow<List<DisplayRoutine>?> =
        combine(
            cashedRoutinesFlow,
            _hideNotDueRoutinesFlow,
        ) { cashedRoutines, hideNotDueRoutines ->
            cashedRoutines.filter {
                if (hideNotDueRoutines) {
                    it.status in dueOrCompletedStatuses
                } else {
                    it.status !in nonExistentStatuses
                }
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000),
        )

    init {
        viewModelScope.launch {
            allRoutinesFlow.update { getAllHabits() }
            updateRoutinesForDate(_currentDateFlow.value)
        }
    }

    private fun updateRoutinesForDate(date: LocalDate) = viewModelScope.launch {
        for (routine in allRoutinesFlow.value) {
            cashedRoutinesFlow.update { cashedRoutines ->
                cashedRoutines.toMutableList().apply {
                    val routineToUpdateIndex = indexOfFirst { it.id == routine.id }
                    val displayRoutine = getDisplayRoutine(habit = routine, date = date)
                    if (routineToUpdateIndex == -1) {
                        add(displayRoutine)
                    } else {
                        set(routineToUpdateIndex, displayRoutine)
                    }
                }
            }
        }
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
            insertHabitCompletion(
                habitId = routineId,
                completionRecord = completionRecord,
                today = todayFlow.value,
            )

            val routine = allRoutinesFlow.value.find { it.id == routineId }?.let {
                getDisplayRoutine(
                    habit = it,
                    date = completionRecord.date,
                )
            }

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
        }
    }

    fun onDateChange(newDate: LocalDate) {
        _currentDateFlow.update { newDate }
        updateRoutinesForDate(newDate)
    }

    fun onNotDueRoutinesVisibilityToggle() {
        _hideNotDueRoutinesFlow.update { !it }
    }

    // TODO update todayFlow when the date changes (in case the screen is opened at midnight)
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