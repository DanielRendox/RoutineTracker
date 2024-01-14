package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

internal interface StreakComputer {
    fun computeAllStreaks(today: LocalDate): List<Streak>
}