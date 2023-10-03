package com.rendox.routinetracker.core.database.model

import com.rendox.routinetracker.core.database.routine.RoutineEntity
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule

internal fun RoutineEntity.toYesNoRoutine(
    schedule: Schedule,
) = Routine.YesNoRoutine(
    id = this.id,
    name = this.name,
    startDate = this.startDate,
    backlogEnabled = this.backlogEnabled,
    periodSeparation = this.periodSeparation,
    vacationStartDate = this.vacationStartDate,
    vacationEndDate = this.vacationEndDate,
    schedule = schedule,
)