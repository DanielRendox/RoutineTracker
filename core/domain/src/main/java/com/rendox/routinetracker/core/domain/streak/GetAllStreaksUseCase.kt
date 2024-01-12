package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.domain.completion_history.HabitComputeStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.isDue
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.completedStatuses
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetAllStreaksUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val computeHabitStatus: HabitComputeStatusUseCase,
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, today: LocalDate): List<DisplayStreak> {
        val habit = habitRepository.getHabitById(habitId)
        val streaks = mutableListOf<Streak>()

        var completedDate = habit.schedule.startDate.minus(DatePeriod(days = 1))
        var failedDate: LocalDate? = null
        while (true) {
            completedDate = completionHistoryRepository.getFirstCompletedRecord(
                habitId = habitId,
                minDate = completedDate.plusDays(1),
            )?.date ?: break

            val habitStatus = computeHabitStatus(
                habitId = habitId,
                validationDate = completedDate,
                today = today,
            )
            if (habitStatus !in completedStatuses) continue

            if (failedDate == null || failedDate < completedDate) {
                failedDate = findNextFailedDate(
                    habit = habit,
                    currentDate = completedDate,
                    today = today,
                )
                if (failedDate == null && streaks.isNotEmpty() && streaks.last().endDate == null) {
                    break
                }
            } else {
                continue
            }

            streaks.add(
                Streak(
                    startDate = completedDate,
                    endDate = failedDate?.minus(DatePeriod(days = 1))
                )
            )
        }

        return streaks.map { it.toDisplayStreak(habit, today) }
    }

    private suspend fun findNextFailedDate(
        habit: Habit,
        currentDate: LocalDate,
        today: LocalDate,
    ): LocalDate? {
        for (date in currentDate..today) {
            if (habit.schedule.isDue(validationDate = date)) {
                val habitStatus = computeHabitStatus(
                    habitId = habit.id!!,
                    validationDate = date,
                    today = today,
                )
                if (habitStatus == HabitStatus.Failed) return date
            }
        }
        return null
    }

    private suspend fun Streak.toDisplayStreak(habit: Habit, today: LocalDate): DisplayStreak {
        val streakEndDate = endDate
        if (streakEndDate != null) {
            return DisplayStreak(
                startDate = startDate,
                endDate = streakEndDate,
            )
        }

        val habitEndDate = habit.schedule.endDate
        val endDate = when {
            habitEndDate != null && habitEndDate < today -> habitEndDate
            computeHabitStatus(
                habitId = habit.id!!,
                validationDate = today,
                today = today
            ) == HabitStatus.Planned -> today.minus(DatePeriod(days = 1))

            else -> today
        }
        return DisplayStreak(
            startDate = startDate,
            endDate = endDate,
        )
    }
}