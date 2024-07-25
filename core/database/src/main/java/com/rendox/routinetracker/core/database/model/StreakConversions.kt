package com.rendox.routinetracker.core.database.model

import com.rendox.routinetracker.core.database.CashedStreakEntity
import com.rendox.routinetracker.core.database.StreakCashedPeriodEntity
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Streak

fun CashedStreakEntity.toExternalModel() = Streak(
    startDate = startDate,
    endDate = endDate,
)

fun StreakCashedPeriodEntity.toExternalModel() = LocalDateRange(
    start = startDate,
    endInclusive = endDate,
)