package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.streak.StreakRepository
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.completedStatuses
import com.rendox.routinetracker.core.model.failedStatuses
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class StartStreakOrJoinStreaksUseCase(
    private val streakRepository: StreakRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
) {
    suspend operator fun invoke(habit: Habit, date: LocalDate) {
        val previousStreak = streakRepository.getStreakByDate(
            routineId = habit.id!!,
            dateWithinStreak = date.minus(DatePeriod(days = 1)),
        )
        val nextCompletedEntry = completionHistoryRepository.getFirstHistoryEntryByStatus(
            routineId = habit.id!!,
            matchingStatuses = completedStatuses,
            minDate = date.plusDays(1),
        )

        val firstFailedStatusAfterCurrentDate =
            completionHistoryRepository.getFirstHistoryEntryByStatus(
                routineId = habit.id!!,
                matchingStatuses = failedStatuses,
                minDate = date.plusDays(1),
            )

        val nextStreak: Streak? = nextCompletedEntry?.let {
            streakRepository.getStreakByDate(
                routineId = habit.id!!,
                dateWithinStreak = it.date,
            )
        }

        if (previousStreak != null) {
            if (nextStreak != null && (firstFailedStatusAfterCurrentDate == null || firstFailedStatusAfterCurrentDate.date > nextStreak.startDate)) {
                streakRepository.updateStreakById(
                    id = previousStreak.id!!,
                    start = previousStreak.startDate,
                    end = nextStreak.endDate,
                )
                if (previousStreak.id != nextStreak.id) {
                    streakRepository.deleteStreakById(id = nextStreak.id!!)
                }
            } else {
                streakRepository.updateStreakById(
                    id = previousStreak.id!!,
                    start = previousStreak.startDate,
                    end = firstFailedStatusAfterCurrentDate
                        ?.date?.minus(DatePeriod(days = 1)),
                )
            }
        } else {
            val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
                routineId = habit.id!!,
                matchingStatuses = listOf(HistoricalStatus.NotCompleted),
            )
            val streakStart =
                if (lastNotCompleted == null) habit.schedule.startDate else date

            if (nextStreak != null && (firstFailedStatusAfterCurrentDate == null || firstFailedStatusAfterCurrentDate.date > nextStreak.startDate)) {
                streakRepository.updateStreakById(
                    id = nextStreak.id!!,
                    start = streakStart,
                    end = nextStreak.endDate,
                )
            } else {
                streakRepository.insertStreak(
                    routineId = habit.id!!,
                    streak = Streak(
                        startDate = streakStart,
                        endDate = firstFailedStatusAfterCurrentDate
                            ?.date?.minus(DatePeriod(days = 1)),
                    ),
                )
            }
        }
    }
}