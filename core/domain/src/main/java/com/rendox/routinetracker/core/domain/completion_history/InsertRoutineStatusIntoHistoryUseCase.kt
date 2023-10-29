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

class InsertRoutineStatusIntoHistoryUseCase(
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
        val planningStatus = routine.computePlanningStatus(currentDate)!!
        println("$currentDate: $planningStatus")
        val historicalStatusData = when (planningStatus) {
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
                deriveHistoricalStatusFromOnVacationStatus(completedOnCurrentDate)
        }

        completionHistoryRepository.insertHistoryEntry(
            routineId = routine.id!!,
            entry = CompletionHistoryEntry(
                currentDate, historicalStatusData.historicalStatus
            ),
            scheduleDeviationIncrementAmount = historicalStatusData.scheduleDeviationIncrementAmount,
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
            routineRepository.updateScheduleDeviation(0, routineId)
        }
    }

    private fun deriveHistoricalStatusFromPlannedStatus(
        schedule: Schedule, completed: Boolean, currentDate: LocalDate, backlogEnabled: Boolean
    ): HistoricalStatusData = if (completed) {
        println("was planned and fully completed")
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
        completionHistoryRepository.updateHistoryEntryStatusByStatus(
            routineId = routineId,
            newStatus = HistoricalStatus.CompletedLater,
            scheduleDeviationIncrementAmount = 0,
            matchingStatuses = listOf(HistoricalStatus.NotCompleted),
        )

        println("backlog sorted out")
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

    private fun deriveHistoricalStatusFromOnVacationStatus(
        completed: Boolean
    ): HistoricalStatusData = if (completed) {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 1,
            historicalStatus = HistoricalStatus.OverCompletedOnVacation,
        )
    } else {
        HistoricalStatusData(
            scheduleDeviationIncrementAmount = 0,
            historicalStatus = HistoricalStatus.OnVacation,
        )
    }

    private data class HistoricalStatusData(
        val scheduleDeviationIncrementAmount: Int,
        val historicalStatus: HistoricalStatus,
    )
}