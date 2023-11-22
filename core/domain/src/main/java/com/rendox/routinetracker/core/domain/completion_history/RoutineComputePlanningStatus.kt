package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate

fun Routine.computePlanningStatus(
    validationDate: LocalDate,
    currentScheduleDeviation: Double,
    actualDate: LocalDate?,
    numOfTimesCompletedInCurrentPeriod: Double,
): PlanningStatus? {
    if (validationDate < schedule.routineStartDate) return null
    schedule.routineEndDate?.let { if (validationDate > it) return null }
    if (isCurrentlyOnVacation(validationDate)) return PlanningStatus.OnVacation

    return when (this) {
        is Routine.YesNoRoutine -> yesNoRoutineComputePlanningStatus(
            validationDate,
            currentScheduleDeviation,
            actualDate,
            numOfTimesCompletedInCurrentPeriod,
        )
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
    currentScheduleDeviation: Double,
    actualDate: LocalDate?,
    numOfTimesCompletedInCurrentPeriod: Double,
): PlanningStatus {
    if (schedule.isDue(validationDate, actualDate, numOfTimesCompletedInCurrentPeriod)) {
        if (currentScheduleDeviation > 0 && schedule.cancelDuenessIfDoneAhead) {
            actualDate?.let {
                var dueDaysCounter = 0
                for (day in it.plusDays(1)..validationDate) {
                    if (
                        schedule.isDue(validationDate, it, numOfTimesCompletedInCurrentPeriod)
                    ) dueDaysCounter++
                }
                if (currentScheduleDeviation >= dueDaysCounter)
                    return PlanningStatus.AlreadyCompleted
            }
        }

        return PlanningStatus.Planned
    }

    if (currentScheduleDeviation < 0 && schedule.backlogEnabled) {
        actualDate?.let {
            var notDueDaysCounter = 0
            for (day in it.plusDays(1)..validationDate) {
                if (
                    !schedule.isDue(day, it, numOfTimesCompletedInCurrentPeriod)
                ) notDueDaysCounter++
            }
            if (currentScheduleDeviation <= -notDueDaysCounter) {
                return PlanningStatus.Backlog
            }
        }
    }

    return PlanningStatus.NotDue
}