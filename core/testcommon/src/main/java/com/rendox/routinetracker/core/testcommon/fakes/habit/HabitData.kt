package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.flow.MutableStateFlow

class HabitData {
    var listOfHabits = MutableStateFlow(emptyList<Habit>())
    val completionHistory = MutableStateFlow(emptyList<Pair<Long, Habit.CompletionRecord>>())
    val vacationHistory = MutableStateFlow(emptyList<Pair<Long, Vacation>>())
    val streaks = MutableStateFlow(emptyList<Pair<Long, Streak>>())
}