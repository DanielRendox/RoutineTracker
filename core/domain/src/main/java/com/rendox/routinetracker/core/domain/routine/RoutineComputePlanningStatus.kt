package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.domain.routine.schedule.isDue
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
    if (schedule.isDue(validationDate)) {
        if (scheduleDeviation > 0) {
            schedule.lastDateInHistory?.let {
                var dueDaysCounter = 0
                for (day in it.plusDays(1)..validationDate) {
                    if (schedule.isDue(day)) dueDaysCounter++
                }
                if (scheduleDeviation >= dueDaysCounter) return PlanningStatus.AlreadyCompleted
            }
        }

        return PlanningStatus.Planned
    }

    if (scheduleDeviation < 0) {
        schedule.lastDateInHistory?.let {
            var notDueDaysCounter = 0
            for (day in it.plusDays(1)..validationDate) {
                if (!schedule.isDue(day)) notDueDaysCounter++
            }
            if (scheduleDeviation <= -notDueDaysCounter) {
                return PlanningStatus.Backlog
            }
        }
    }

    return PlanningStatus.NotDue
}