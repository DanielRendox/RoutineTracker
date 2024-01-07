package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
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
    private val insertHabitCompletion: InsertHabitCompletionUseCase,
    private val computeHabitStatus: HabitComputeStatusUseCase,
    private val completionHistoryRepository: CompletionHistoryRepository,
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
                    if (hideNotDueRoutines) {
                        it.status in dueOrCompletedStatuses
                    } else {
                        it.status !in nonExistentStatuses
                    }
                }.sortedBy { it.id }
            }
        }.stateIn(
            scope = viewModelScope,
            initialValue = null,
            started = SharingStarted.WhileSubscribed(5_000),
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
                        it[date] = existingRoutines.toMutableList().apply { add(routineForDate) }
                    }
                }
            }
        }
    }

    private suspend fun getDisplayRoutine(habit: Habit, date: LocalDate): DisplayRoutine {
        val habitStatus: HabitStatus = computeHabitStatus(
            habitId = habit.id!!,
            validationDate = date,
            today = todayFlow.value,
        )
        val numOfTimesCompleted = completionHistoryRepository.getRecordByDate(
            habitId = habit.id!!,
            date = date,
        )?.numOfTimesCompleted ?: 0F

        return DisplayRoutine(
            name = habit.name,
            id = habit.id!!,
            type = DisplayRoutineType.YesNoHabit,
            status = habitStatus,
            numOfTimesCompleted = numOfTimesCompleted,
            completionTime = null,
            hasGrayedOutLook = habitStatus !in dueOrCompletedStatuses,
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

    // TODO update todayFlow when the date changes (in case the screen is opened at midnight)

    companion object {
        private val dueOrCompletedStatuses = listOf(
            HabitStatus.Planned,
            HabitStatus.Backlog,
            HabitStatus.Failed,
            HabitStatus.Completed,
            HabitStatus.OverCompleted,
            HabitStatus.SortedOutBacklog,
        )

        private val nonExistentStatuses = listOf(
            HabitStatus.NotStarted,
            HabitStatus.Finished,
        )
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