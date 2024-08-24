package com.rendox.routinetracker.core.database.model

import com.rendox.routinetracker.core.database.CachedStreakEntity
import com.rendox.routinetracker.core.model.Streak

fun CachedStreakEntity.toExternalModel() = Streak(
    startDate = startDate,
    endDate = endDate,
)