package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.routine_completion_history.RoutineCompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.HabitRepository
import com.rendox.routinetracker.core.data.streak.StreakRepository
import com.rendox.routinetracker.core.model.DisplayStreak
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetDisplayStreaksUseCase(
    private val streakRepository: StreakRepository,
    private val routineCompletionHistoryRepository: RoutineCompletionHistoryRepository,
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(
        routineId: Long,
        afterDateInclusive: LocalDate? = null,
        beforeDateInclusive: LocalDate? = null,
        today: LocalDate,
    ): List<DisplayStreak> = streakRepository.getAllStreaks(
        routineId = routineId,
        afterDateInclusive = afterDateInclusive,
        beforeDateInclusive = beforeDateInclusive,
    ).map {
        val endDate = it.endDate
        val streakEnd: LocalDate = if (endDate != null) {
            endDate
        } else {
            val routineEndDate =
                habitRepository.getHabitById(routineId).schedule.endDate
            if (routineEndDate != null && routineEndDate < today) {
                routineEndDate
            } else {
                val completedToday =
                    routineCompletionHistoryRepository.getLastHistoryEntry(routineId)?.date == today
                if (completedToday) today else today.minus(DatePeriod(days = 1))
            }
        }

        DisplayStreak(
            startDate = it.startDate,
            endDate = streakEnd,
        )
    }
}