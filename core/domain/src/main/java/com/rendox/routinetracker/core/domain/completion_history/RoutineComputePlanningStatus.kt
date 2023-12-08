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
    scheduleDeviationInCurrentPeriod: Double?,
    lastVacationEndDate: LocalDate?,
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
            scheduleDeviationInCurrentPeriod,
            lastVacationEndDate,
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
    scheduleDeviationInCurrentPeriod: Double?,
    lastVacationEndDate: LocalDate?,
    ): PlanningStatus {
    println("RoutineComputePlanningStatus: validation date = $validationDate")
//    if (
//        schedule.isDue(
//            validationDate,
//            actualDate,
//            numOfTimesCompletedInCurrentPeriod,
//            scheduleDeviationInCurrentPeriod,
//            lastVacationEndDate,
//        )
//    ) {
//        println("RoutineComputePlanningStatus: currentScheduleDeviation = $currentScheduleDeviation")
//        println("RoutineComputePlanningStatus: cancelDuenessIfDoneAhead = ${schedule.cancelDuenessIfDoneAhead}")
//        if (currentScheduleDeviation > 0 && schedule.cancelDuenessIfDoneAhead) {
//            actualDate?.let {
//                var dueDaysCounter = 0
//                for (day in it.plusDays(1)..validationDate) {
//                    if (
//                        schedule.isDue(
//                            validationDate,
//                            it,
//                            numOfTimesCompletedInCurrentPeriod,
//                            scheduleDeviationInCurrentPeriod
//                        )
//                    ) dueDaysCounter++
//                }
//                println("due days counter = $dueDaysCounter")
//                if (currentScheduleDeviation >= dueDaysCounter)
//                    return PlanningStatus.AlreadyCompleted
//            }
//        }

//        return PlanningStatus.Planned
//    }
//
//    println("RoutineComputePlanningStatus: backlog enabled = ${schedule.backlogEnabled}")
//    println()
//    if (currentScheduleDeviation < 0 && schedule.backlogEnabled) {
//        actualDate?.let {
//            var notDueDaysCounter = 0
//            for (day in it.plusDays(1)..validationDate) {
//                if (
//                    !schedule.isDue(
//                        day,
//                        it,
//                        numOfTimesCompletedInCurrentPeriod,
//                        scheduleDeviationInCurrentPeriod
//                    )
//                ) notDueDaysCounter++
//            }
//            if (currentScheduleDeviation <= -notDueDaysCounter) {
//                return PlanningStatus.Backlog
//            }
//        }
//    }
//
//    return PlanningStatus.NotDue
    TODO()
}