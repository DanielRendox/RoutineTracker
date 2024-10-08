package com.rendox.routinetracker.core.domain.streak.computer

import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

interface StreakComputer {
    fun computeStreaks(
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): List<Streak>
}