package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.streak.StreakRepository
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.completedStatuses
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class BreakStreakUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val streakRepository: StreakRepository,
) {
    suspend operator fun invoke(routineId: Long, date: LocalDate) {
        val oldStreak = streakRepository.getStreakByDate(
            routineId = routineId,
            dateWithinStreak = date,
        )

        if (oldStreak != null) {
            streakRepository.deleteStreakById(id = oldStreak.id!!)

            if (oldStreak.startDate != date) {
                streakRepository.insertStreak(
                    routineId = routineId,
                    streak = Streak(
                        startDate = oldStreak.startDate,
                        endDate = date.minus(DatePeriod(days = 1)),
                    ),
                )
            }

            val completedEntry = completionHistoryRepository.getFirstHistoryEntryByStatus(
                routineId = routineId,
                matchingStatuses = completedStatuses,
                minDate = date.plusDays(1),
            )

            if (completedEntry != null) {
                streakRepository.insertStreak(
                    routineId = routineId,
                    streak = Streak(
                        startDate = completedEntry.date,
                        endDate = oldStreak.endDate,
                    )
                )
            }
        }
    }
}