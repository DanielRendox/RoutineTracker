package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.Routine

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

    var listOfRoutines = emptyList<Routine>()
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
}