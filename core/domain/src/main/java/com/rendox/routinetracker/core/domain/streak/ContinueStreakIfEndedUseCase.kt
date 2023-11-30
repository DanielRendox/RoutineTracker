package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.streak.StreakRepository
import kotlinx.datetime.LocalDate

class ContinueStreakIfEndedUseCase(
    private val streakRepository: StreakRepository
) {
    suspend operator fun invoke(routineId: Long, currentDate: LocalDate) {
        val latestStreak = streakRepository.getLastStreak(routineId)
        if (latestStreak?.endDate == currentDate) {
            streakRepository.updateStreakById(
                id = latestStreak.id!!,
                start = latestStreak.startDate,
                end = null,
            )
        }
    }
}