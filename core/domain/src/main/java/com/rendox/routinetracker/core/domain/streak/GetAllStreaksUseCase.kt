package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

interface GetAllStreaksUseCase {
    suspend operator fun invoke(
        habitId: Long,
        today: LocalDate,
    ): List<Streak>
}