package com.rendox.routinetracker.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputer
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.coroutines.CoroutineContext

class AgendaScreenViewModel(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    private val habitRepository: HabitRepository,
    private val insertHabitCompletion: InsertHabitCompletionUseCase,
    private val vacationRepository: VacationRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val defaultDispatcher: CoroutineContext = Dispatchers.Default,
) : ViewModel() {
    private val todayFlow = MutableStateFlow(today)
    private val allRoutinesFlow = MutableStateFlow(emptyList<Habit>())
    private val completionHistoryFlow =
        MutableStateFlow(emptyList<Pair<Long, Habit.CompletionRecord>>())
    private val vacationHistoryFlow = MutableStateFlow(emptyList<Pair<Long, Vacation>>())
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
            val jobs = mutableListOf<Job>()

            val updateRoutinesJob = launch {
                allRoutinesFlow.update { habitRepository.getAllHabits() }
            }
            jobs.add(updateRoutinesJob)

            val updateCompletionHistoryJob = launch {
                completionHistoryFlow.update { completionHistoryRepository.getAllRecords() }
            }
            jobs.add(updateCompletionHistoryJob)

            val updateVacationsJob = launch {
                vacationHistoryFlow.update { vacationRepository.getAllVacations() }
            }
            jobs.add(updateVacationsJob)

            jobs.joinAll()
            updateRoutinesForDate(_currentDateFlow.value)
        }
    }

    private fun updateRoutinesForDate(date: LocalDate) = viewModelScope.launch(defaultDispatcher) {
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
        val habitStatusComputer = HabitStatusComputer(
            habit = habit,
            completionHistory = completionHistoryFlow.value
                .filter { it.first == habit.id }
                .map { it.second },
            vacationHistory = vacationHistoryFlow.value
                .filter { it.first == habit.id }
                .map { it.second },
            defaultDispatcher = defaultDispatcher,
        )
        val habitStatus: HabitStatus = habitStatusComputer.computeStatus(
            validationDate = date,
            today = todayFlow.value,
        )

        val numOfTimesCompleted = completionHistoryFlow.value.find {
            it.first == habit.id && it.second.date == date
        }?.second?.numOfTimesCompleted ?: 0F

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
            completionHistoryFlow.update { completionHistoryRepository.getAllRecords() }

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