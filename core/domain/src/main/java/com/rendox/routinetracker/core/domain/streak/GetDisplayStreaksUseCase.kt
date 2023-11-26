package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.data.streak.StreakRepository
import com.rendox.routinetracker.core.model.DisplayStreak
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetDisplayStreaksUseCase(
    private val streakRepository: StreakRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val routineRepository: RoutineRepository,
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
                routineRepository.getRoutineById(routineId).schedule.routineEndDate
            if (routineEndDate != null && routineEndDate < today) {
                routineEndDate
            } else {
                val completedToday =
                    completionHistoryRepository.getLastHistoryEntry(routineId)?.date == today
                if (completedToday) today else today.minus(DatePeriod(days = 1))
            }
        }

        DisplayStreak(
            startDate = it.startDate,
            endDate = streakEnd,
        )
    }
}