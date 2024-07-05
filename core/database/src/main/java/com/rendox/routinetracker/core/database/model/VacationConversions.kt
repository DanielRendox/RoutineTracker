package com.rendox.routinetracker.core.database.model

import com.rendox.routinetracker.core.database.VacationEntity
import com.rendox.routinetracker.core.model.Vacation

fun VacationEntity.toExternalModel() = Vacation(
    startDate = startDate,
    endDate = endDate,
)