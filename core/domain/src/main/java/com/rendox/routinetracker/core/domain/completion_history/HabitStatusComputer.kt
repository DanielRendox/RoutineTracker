package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

interface HabitStatusComputer {
    fun computeStatus(
        validationDate: LocalDate,
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): HabitStatus
}