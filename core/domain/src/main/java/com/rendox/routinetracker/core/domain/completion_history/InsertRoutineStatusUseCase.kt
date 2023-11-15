package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.routine.computePlanningStatus
import com.rendox.routinetracker.core.domain.routine.schedule.getPeriodRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

class InsertRoutineStatusUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val routineRepository: RoutineRepository,
) {

    suspend operator fun invoke(
        routineId: Long,
        currentDate: LocalDate,
        completedOnCurrentDate: Boolean,
    ) {
        val routine = routineRepository.getRoutineById(routineId)
        when (routine) {
            is Routine.YesNoRoutine -> insertYesNoRoutineStatus(
                routine, currentDate, completedOnCurrentDate
            )
        }

        if (routine.schedule is Schedule.PeriodicSchedule) {
            nullifyScheduleDeviationIfNecessary(
                routine.id!!,
                routine.schedule as Schedule.PeriodicSchedule,
                currentDate
            )
        }
    }

    private suspend fun insertYesNoRoutineStatus(
        routine: Routine.YesNoRoutine,
        currentDate: LocalDate,
        completedOnCurrentDate: Boolean,
    ) {
        val lastHistoryEntry = completionHistoryRepository.getLastHistoryEntry(routine.id!!)
        val currentScheduleDeviation = lastHistoryEntry?.currentScheduleDeviation ?: 0
        val historicalStatusData = when (
            routine.computePlanningStatus(
                validationDate = currentDate,
                currentScheduleDeviation = currentScheduleDeviation,
                dateScheduleDeviationIsActualFor = lastHistoryEntry?.date,
            )!!
        ) {
            PlanningStatus.Planned -> deriveHistoricalStatusFromPlannedStatus(
                routine.schedule,
                completedOnCurrentDate,
                currentDate,
                routine.schedule.backlogEnabled
            )

            PlanningStatus.Backlog -> deriveHistoricalStatusFromBacklogStatus(
                completedOnCurrentDate, routine.id!!
            )

            PlanningStatus.AlreadyCompleted -> deriveHistoricalStatusFromAlreadyCompletedStatus(
                completedOnCurrentDate
            )

            PlanningStatus.NotDue -> deriveHistoricalStatusFromNotDueStatus(
                completedOnCurrentDate, routine.schedule.cancelDuenessIfDoneAhead
            )

            PlanningStatus.OnVacation ->
                deriveHistoricalStatusFromOnVacationStatus(
                    completedOnCurrentDate, routine.id!!, currentScheduleDeviation
                )
        }

        val schedule = routine.schedule
        val currentDateIsPeriodEnd = if (schedule is Schedule.PeriodicSchedule) {
            currentDate == schedule.getPeriodRange(currentDate).endInclusive
        } else false
        val shouldNullifyScheduleDeviation =
            currentDateIsPeriodEnd && (schedule as Schedule.PeriodicSchedule).periodSeparationEnabled
        val scheduleDeviation = if (shouldNullifyScheduleDeviation) {
            0
        } else {
            completionHistoryRepository
                .getLastHistoryEntry(routineId = routine.id!!)
                ?.currentScheduleDeviation
                ?.plus(historicalStatusData.scheduleDeviationIncrementAmount) ?: 0
        }

        completionHistoryRepository.insertHistoryEntry(
            routineId = routine.id!!,
            entry = CompletionHistoryEntry(
                date = currentDate,
                status = historicalStatusData.historicalStatus,
                currentScheduleDeviation = scheduleDeviation,
            ),
        )
    }

    private suspend fun nullifyScheduleDeviationIfNecessary(
        routineId: Long,
        schedule: Schedule.PeriodicSchedule,
        currentDate: LocalDate
    ) {
        val currentDateIsPeriodEnd =
            currentDate == schedule.getPeriodRange(currentDate).endInclusive

        if (currentDateIsPeriodEnd && schedule.periodSeparationEnabled) {
            val lastInsertedEntry = completionHistoryRepository.getLastHistoryEntry(routineId)!!
            completionHistoryRepository.updateHistoryEntryStatusByDate(
                routineId = routineId,
                date = lastInsertedEntry.date,
                newStatus = lastInsertedEntry.status,
                newScheduleDeviation = 0,
            )
        }
    }

    private fun deriveHistoricalStatusFromPlannedStatus(
        schedule: Schedule, completed: Boolean, currentDate: LocalDate, backlogEnabled: Boolean
    ): HistoricalStatusData = if (completed) {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 0,
            historicalStatus = HistoricalStatus.Completed,
        )
    } else {
        if (schedule is Schedule.ByNumOfDueDays) {
            val numOfDueDaysSchedule = schedule as Schedule.ByNumOfDueDays
            val numOfCompletedDays = when (numOfDueDaysSchedule) {
                is Schedule.WeeklyScheduleByNumOfDueDays ->
                    numOfDueDaysSchedule.numOfCompletedDaysInCurrentPeriod

                is Schedule.MonthlyScheduleByNumOfDueDays ->
                    numOfDueDaysSchedule.numOfCompletedDaysInCurrentPeriod

                is Schedule.AnnualScheduleByNumOfDueDays ->
                    numOfDueDaysSchedule.numOfCompletedDaysInCurrentPeriod

                is Schedule.PeriodicCustomSchedule ->
                    numOfDueDaysSchedule.numOfCompletedDaysInCurrentPeriod
            }
            val validationDatePeriod =
                (numOfDueDaysSchedule as Schedule.PeriodicSchedule).getPeriodRange(currentDate)
            val numOfDueDays =
                numOfDueDaysSchedule.getNumOfDueDatesInPeriod(validationDatePeriod)

            val numOfDaysThatRemainToBeCompletedInPeriod = numOfDueDays - numOfCompletedDays
            val daysRemainingInCurrentPeriod =
                currentDate.daysUntil(validationDatePeriod.endInclusive) + 1 // including today
            val thereAreEnoughDaysInPeriodToCompleteLater =
                numOfDaysThatRemainToBeCompletedInPeriod < daysRemainingInCurrentPeriod

            if (thereAreEnoughDaysInPeriodToCompleteLater) {
                HistoricalStatusData(
                    scheduleDeviationIncrementAmount = 0,
                    historicalStatus = HistoricalStatus.Skipped,
                )
            } else {
                HistoricalStatusData(
                    scheduleDeviationIncrementAmount = if (backlogEnabled) -1 else 0,
                    historicalStatus = HistoricalStatus.NotCompleted,
                )
            }
        } else {
            HistoricalStatusData(
                scheduleDeviationIncrementAmount = if (backlogEnabled) -1 else 0,
                historicalStatus = HistoricalStatus.NotCompleted,
            )
        }
    }

    private suspend fun deriveHistoricalStatusFromBacklogStatus(
        completed: Boolean, routineId: Long
    ): HistoricalStatusData = if (completed) {
        sortOutBacklog(routineId, completionHistoryRepository)

        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 1,
            historicalStatus = HistoricalStatus.SortedOutBacklog,
        )
    } else {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 0,
            historicalStatus = HistoricalStatus.Skipped,
        )
    }

    private fun deriveHistoricalStatusFromAlreadyCompletedStatus(
        completed: Boolean
    ): HistoricalStatusData = if (completed) {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 0,
            historicalStatus = HistoricalStatus.Completed
        )
    } else {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = -1,
            historicalStatus = HistoricalStatus.AlreadyCompleted
        )
    }

    private fun deriveHistoricalStatusFromNotDueStatus(
        completed: Boolean, cancelDuenessIfDoneAhead: Boolean
    ): HistoricalStatusData = if (completed) {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = if (cancelDuenessIfDoneAhead) 1 else 0,
            historicalStatus = HistoricalStatus.OverCompleted,
        )
    } else {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 0,
            historicalStatus = HistoricalStatus.Skipped,
        )
    }

    private suspend fun deriveHistoricalStatusFromOnVacationStatus(
        completed: Boolean,
        routineId: Long,
        currentScheduleDeviation: Int,
    ): HistoricalStatusData = if (completed) {
        if (currentScheduleDeviation < 0) {
            sortOutBacklog(routineId, completionHistoryRepository)
            HistoricalStatusData(
                scheduleDeviationIncrementAmount = 1,
                historicalStatus = HistoricalStatus.SortedOutBacklogOnVacation,
            )
        } else {
            HistoricalStatusData(
                scheduleDeviationIncrementAmount = 1,
                historicalStatus = HistoricalStatus.OverCompletedOnVacation,
            )
        }
    } else {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 0,
            historicalStatus = HistoricalStatus.NotCompletedOnVacation,
        )
    }

    private data class HistoricalStatusData(
        val scheduleDeviationIncrementAmount: Int,
        val historicalStatus: HistoricalStatus,
    )
}