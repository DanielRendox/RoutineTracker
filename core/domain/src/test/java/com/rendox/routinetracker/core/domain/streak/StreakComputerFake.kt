package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

class StreakComputerFake(
    private val streaks: List<Streak>,
) : StreakComputer {
    override fun computeAllStreaks(
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>
    ): List<Streak> = streaks.filter { it.endDate <= today }

    override fun computeStreaksInPeriod(
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
        period: LocalDateRange
    ): List<Streak> = streaks.filter { streak ->
        streak.startDate <= period.endInclusive && streak.endDate >= period.start
                && streak.endDate <= today
    }
}