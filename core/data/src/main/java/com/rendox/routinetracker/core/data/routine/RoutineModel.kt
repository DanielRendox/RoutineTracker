package com.rendox.routinetracker.core.data.routine

import com.rendox.routinetracker.core.database.routine.RoutineEntity
import com.rendox.routinetracker.core.model.Routine

fun RoutineEntity.asExternalModel() = Routine(
    id = id,
    type = type,
    name = name,
    startDate = startDate,
    backlogEnabled = backlogEnabled,
    periodSeparation = periodSeparation,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
)