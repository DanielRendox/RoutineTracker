package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class HabitData {
    private val completionHistoryLock = Any()
    var routineCompletionHistory = emptyList<Pair<Long, CompletionHistoryEntry>>()
        get() {
            synchronized(completionHistoryLock) {
                return field
            }
        }
        set(value) {
            synchronized(completionHistoryLock) {
                field = value
            }
        }

    var listOfHabits = MutableStateFlow(emptyList<Habit>())

    private val completedLaterHistoryLock = Any()
    var completedLaterHistory = emptyList<Pair<Long, LocalDate>>()
        get() {
            synchronized(completedLaterHistoryLock) {
                return field
            }
        }
        set(value) {
            synchronized(completedLaterHistoryLock) {
                field = value
            }
        }

    private val dueDateCompletionTimeLock = Any()
    var dueDateCompletionTimes = emptyList<DueDateCompletionTimeEntity>()
        get() {
            synchronized(dueDateCompletionTimeLock) {
                return field
            }
        }
        set(value) {
            synchronized(dueDateCompletionTimeLock) {
                field = value
            }
        }

    private val listOfStreaksLock = Any()
    var listOfStreaks = emptyList<Pair<Long, Streak>>()
        get() {
            synchronized(listOfStreaksLock) {
                return field
            }
        }
        set(value) {
            synchronized(listOfStreaksLock) {
                field = value
            }
        }

    val completionHistory = MutableStateFlow(emptyList<Triple<Long, LocalDate, Float>>())
    val vacationHistory = MutableStateFlow(emptyList<Pair<Long, Vacation>>())
}

data class DueDateCompletionTimeEntity(
    val routineId: Long,
    val dueDateNumber: Int,
    val completionTime: LocalTime,
)