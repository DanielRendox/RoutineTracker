package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.model.HabitStatus
import kotlinx.datetime.LocalDate

internal interface HabitStatusComputer {
    fun computeStatus(
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitStatus
}