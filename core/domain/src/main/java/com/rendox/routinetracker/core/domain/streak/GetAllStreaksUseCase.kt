package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputer
import com.rendox.routinetracker.core.domain.completion_history.isDue
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.DisplayStreak
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation
import com.rendox.routinetracker.core.model.completedStatuses
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.coroutines.CoroutineContext

class GetAllStreaksUseCase(
    private val habit: Habit,
    private val completionHistory: List<Habit.CompletionRecord>,
    vacationHistory: List<Vacation>,
    private val defaultDispatcher: CoroutineContext,
) {
    private val habitStatusComputer = HabitStatusComputer(
        habit = habit,
        completionHistory = completionHistory,
        vacationHistory = vacationHistory,
        defaultDispatcher = defaultDispatcher,
    )

    suspend operator fun invoke(
        today: LocalDate
    ): List<DisplayStreak> = withContext(defaultDispatcher) {
        val streaks = mutableListOf<Streak>()

        var completedDate = habit.schedule.startDate.minus(DatePeriod(days = 1))
        var failedDate: LocalDate? = null
        while (true) {
            completedDate = completionHistory
                .filter { it.date > completedDate }
                .minOfOrNull { it.date } ?: break

            val habitStatus = habitStatusComputer.computeStatus(
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

        streaks.map { it.toDisplayStreak(habit, today) }
    }

    private suspend fun findNextFailedDate(
        habit: Habit,
        currentDate: LocalDate,
        today: LocalDate,
    ): LocalDate? {
        for (date in currentDate..today) {
            if (habit.schedule.isDue(validationDate = date)) {
                val habitStatus = habitStatusComputer.computeStatus(
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
            habitStatusComputer.computeStatus(
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