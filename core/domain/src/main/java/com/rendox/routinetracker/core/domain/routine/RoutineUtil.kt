package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.model.CompletableStatus
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun Routine.computeStatus(
    validationDate: LocalDate,
    completionHistory: List<HistoricalStatus>,
): CompletableStatus {
    val validationDateIndex = schedule.startDate.daysUntil(validationDate)
    if (validationDateIndex < 0) return PlanningStatus.Unknown

    if (validationDateIndex in completionHistory.indices) {
        return completionHistory[validationDateIndex]
    }

    if (isCurrentlyOnVacation(validationDate)) return HistoricalStatus.OnVacation

    return computeRoutinePlanningStatus(validationDate)
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

private fun Routine.computeRoutinePlanningStatus(validationDate: LocalDate): PlanningStatus {
    return when (this) {
        is Routine.YesNoRoutine -> yesNoRoutineComputePlanningStatus(validationDate)
    }
}

private fun Routine.YesNoRoutine.yesNoRoutineComputePlanningStatus(
    validationDate: LocalDate
): PlanningStatus {
//    val allPeriods = listOf(schedule.getPeriodRange(validationDate))
//    val periodRange = allPeriods[allPeriods.lastIndex]
//    val periodStartIndex = schedule.startDate.daysUntil(periodRange.start)
//    val periodEndIndex = schedule.startDate.daysUntil(periodRange.endInclusive)
//    val periodCompletionHistory = completionHistory.slice(periodStartIndex..periodEndIndex)
//
//    val daysDone = periodCompletionHistory.count { it == HistoricalStatus.FullyCompleted }
//    val daysToBeDone = periodRange
//        .toList().count { schedule.isDue(it) }

    if (schedule.backlogEnabled && schedule.scheduleDeviation < 0) {
        return PlanningStatus.Planned
    }

    if (schedule.cancelDuenessIfDoneAhead && schedule.scheduleDeviation > 0) {
        return PlanningStatus.NotDue
    }

    return if (schedule.isDue(validationDate)) PlanningStatus.Planned else PlanningStatus.NotDue
}


