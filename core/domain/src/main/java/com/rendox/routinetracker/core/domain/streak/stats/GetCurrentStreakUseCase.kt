package com.rendox.routinetracker.core.domain.streak.stats

import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.streak.StreakManager
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.logic.joinAdjacentStreaks
import com.rendox.routinetracker.core.logic.time.minusDays
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class GetCurrentStreakUseCase(
    private val streakRepository: StreakRepository,
    completionHistoryRepository: CompletionHistoryRepository,
    vacationRepository: VacationRepository,
    streakComputer: StreakComputer,
) : StreakManager(
    completionHistoryRepository,
    vacationRepository,
    streakComputer,
) {
    suspend operator fun invoke(
        habit: Habit,
        today: LocalDate,
    ): Streak? {
        formStreaks(habit = habit, today = today, completion = null)?.let { (period, streaks) ->
            streakRepository.upsertStreaks(habit.id!!, period, streaks)
        }

        val currentPeriod = getPeriodRange(habit.schedule, today)
        val currentPeriodStreaks = computeStreaks(
            habit = habit,
            period = currentPeriod,
            today = today,
            completion = null,
        )
        val lastCachedStreak: Streak? = streakRepository.getLastStreak(habit.id!!)
        return currentPeriodStreaks
            .toMutableList()
            .apply { if (lastCachedStreak != null) add(lastCachedStreak) }
            .joinAdjacentStreaks()
            .maxByOrNull { it.startDate }
            ?.takeIf { it.endDate >= today.minusDays(1) }
    }
}