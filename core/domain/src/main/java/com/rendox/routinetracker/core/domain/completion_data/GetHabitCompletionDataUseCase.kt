package com.rendox.routinetracker.core.domain.completion_data

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.HabitCompletionData
import kotlinx.datetime.LocalDate

interface GetHabitCompletionDataUseCase {
    suspend operator fun invoke(
        habitId: Long,
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitCompletionData

    suspend operator fun invoke(
        habitId: Long,
        validationDates: LocalDateRange,
        today: LocalDate,
    ): Map<LocalDate, HabitCompletionData>
}