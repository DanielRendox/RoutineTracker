package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate

fun Routine.computePlanningStatus(validationDate: LocalDate): PlanningStatus? {
    if (validationDate < schedule.routineStartDate) return null
    schedule.routineEndDate?.let { if (validationDate > it) return null }

    if (isCurrentlyOnVacation(validationDate)) return PlanningStatus.OnVacation
    
    return when (this) {
        is Routine.YesNoRoutine -> yesNoRoutineComputePlanningStatus(validationDate)
    }
}

private fun Routine.isCurrentlyOnVacation(validationDate: LocalDate): Boolean {
    schedule.vacationStartDate?.let {
        if (validationDate < it) return false
    } ?: return false
    schedule.vacationEndDate?.let {
        return validationDate <= it
    }
    return true
}

private fun Routine.YesNoRoutine.yesNoRoutineComputePlanningStatus(
    validationDate: LocalDate,
): PlanningStatus {
    if (schedule.cancelDuenessIfDoneAhead && lastDateInHistory != null) {
        var dueDaysCounter = 0
        for (day in lastDateInHistory!!.plusDays(1)..validationDate) {
            if (schedule.isDue(day)) dueDaysCounter++
        }
        if (scheduleDeviation >= dueDaysCounter) return PlanningStatus.AlreadyCompleted
    }

    if (schedule.isDue(validationDate)) return PlanningStatus.Planned

    // In cases when there is a backlog but the routine is also due on this day because
    // of the schedule, we want to return Planned and not Backlog. That's why the following
    // check isn't performed earlier.
    if (schedule.backlogEnabled && lastDateInHistory != null) {
        var notDueDaysCounter = 0
        for (day in lastDateInHistory!!.plusDays(1)..validationDate) {
            if (!schedule.isDue(day)) notDueDaysCounter++
        }
        if (scheduleDeviation <= notDueDaysCounter) return PlanningStatus.Backlog
    }

    return PlanningStatus.NotDue
}


