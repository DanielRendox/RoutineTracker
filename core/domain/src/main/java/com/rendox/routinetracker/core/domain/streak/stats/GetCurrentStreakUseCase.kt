package com.rendox.routinetracker.core.domain.streak.stats

import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.domain.streak.StreakManager
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class GetCurrentStreakUseCase(
    private val streakManager: StreakManager,
    private val streakRepository: StreakRepository,
) {
    suspend operator fun invoke(
        habit: Habit,
        today: LocalDate,
    ): Streak? {
        streakManager.formLastNotSavedStreaks(habit, today)?.let { (period, streaks) ->
            streakRepository.upsertStreaks(habit.id!!, period, streaks)
        }
        return streakManager.getCurrentStreak(habit, today)
    }
}