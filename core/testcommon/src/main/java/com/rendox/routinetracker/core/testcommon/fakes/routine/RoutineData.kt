package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class RoutineData {
    private val completionHistoryLock = Any()
    var completionHistory = emptyList<Pair<Long, CompletionHistoryEntry>>()
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

    private val listOfRoutinesLock = Any()
    var listOfHabits = emptyList<Habit>()
        get() {
            synchronized(listOfRoutinesLock) {
                return field
            }
        }
        set(value) {
            synchronized(listOfRoutinesLock) {
                field = value
            }
        }

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
}

data class DueDateCompletionTimeEntity(
    val routineId: Long,
    val dueDateNumber: Int,
    val completionTime: LocalTime,
)