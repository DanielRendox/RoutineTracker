package com.rendox.routinetracker.core.database.routine.model

import com.rendox.routinetracker.core.database.routine.RoutineEntity
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule

internal fun RoutineEntity.toYesNoRoutine(
    schedule: Schedule,
) = Routine.YesNoRoutine(
    id = id,
    name = name,
    schedule = schedule,
    scheduleDeviation = scheduleDeviation!!,
)