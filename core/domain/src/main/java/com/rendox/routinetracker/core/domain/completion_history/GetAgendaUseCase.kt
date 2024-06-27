package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitCompletionData
import kotlinx.datetime.LocalDate

interface GetAgendaUseCase {
    suspend operator fun invoke(
        validationDate: LocalDate,
        today: LocalDate,
    ): Map<Habit, HabitCompletionData>
}