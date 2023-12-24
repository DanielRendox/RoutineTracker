package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalTime

class HabitData {
    var listOfHabits = MutableStateFlow(emptyList<Habit>())
    val dueDateCompletionTimes = MutableStateFlow(emptyList<DueDateCompletionTimeEntity>())
    val completionHistory = MutableStateFlow(emptyList<Pair<Long, Habit.CompletionRecord>>())
    val vacationHistory = MutableStateFlow(emptyList<Pair<Long, Vacation>>())
}

data class DueDateCompletionTimeEntity(
    val routineId: Long,
    val dueDateNumber: Int,
    val completionTime: LocalTime,
)