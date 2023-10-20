package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate

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
        nullifyScheduleDeviationIfNecessary(routine, currentDate)

        when (routine) {
            is Routine.YesNoRoutine -> insertYesNoRoutineStatus(
                routine,
                currentDate,
                completedOnCurrentDate
            )
        }
    }

    private suspend fun insertYesNoRoutineStatus(
        routine: Routine.YesNoRoutine,
        currentDate: LocalDate,
        completedOnCurrentDate: Boolean,
    ) {
        val planningStatus = routine.computePlanningStatus(currentDate) ?: throw NullPointerException(
        "It's prohibited to insert routine status for dates that are either earlier " +
                "than start date or later than end date."
        )

        val historicalStatus = if (completedOnCurrentDate) {
            when (planningStatus) {
                PlanningStatus.Planned -> HistoricalStatus.FullyCompleted
                PlanningStatus.Backlog -> {
                    updateLastIncompleteDateWhenBacklogIsSortedOut(
                        routineId = routine.id!!,
                        tasksCompletedCounterIncrementAmount = 0,
                    )
                    HistoricalStatus.FullyCompleted
                }

                PlanningStatus.AlreadyCompleted -> HistoricalStatus.FullyCompleted
                // YesNoRoutine can't have OverCompleted status because the user
                // can't complete more than one task a day
                PlanningStatus.NotDue -> HistoricalStatus.FullyCompleted
                PlanningStatus.OnVacation -> HistoricalStatus.OverCompleted
            }
        } else {
            when (planningStatus) {
                PlanningStatus.Planned -> HistoricalStatus.NotCompleted
                // The user didn't sort out the backlog but the routine is also not due on this day
                PlanningStatus.Backlog -> HistoricalStatus.Skipped
                PlanningStatus.AlreadyCompleted -> HistoricalStatus.Skipped
                PlanningStatus.NotDue -> HistoricalStatus.Skipped
                PlanningStatus.OnVacation -> HistoricalStatus.OnVacation
            }
        }

        insertHistoryEntry(routine, CompletionHistoryEntry(currentDate, historicalStatus))
    }

    private suspend fun insertHistoryEntry(
        routine: Routine.YesNoRoutine,
        entry: CompletionHistoryEntry,
    ) {
        val scheduleDeviationIncrementAmount = when (entry.status) {
            HistoricalStatus.NotCompleted -> -1
            HistoricalStatus.PartiallyCompleted -> throw IllegalArgumentException(
                "YesNoRoutine can't have PartiallyCompleted status"
            )

            HistoricalStatus.FullyCompleted -> 0
            HistoricalStatus.OverCompleted -> 1
            HistoricalStatus.Skipped -> 0
            HistoricalStatus.OnVacation -> 0
        }
        completionHistoryRepository.insertHistoryEntry(
            routineId = routine.id!!,
            entry = entry,
            tasksCompletedCounterIncrementAmount = scheduleDeviationIncrementAmount,
        )
    }

    private suspend fun updateLastIncompleteDateWhenBacklogIsSortedOut(
        routineId: Long, tasksCompletedCounterIncrementAmount: Int
    ) {
        completionHistoryRepository.updateHistoryEntryStatusByStatus(
            routineId = routineId,
            newStatus = HistoricalStatus.Skipped,
            tasksCompletedCounterIncrementAmount = tasksCompletedCounterIncrementAmount,
            matchingStatuses = listOf(
                HistoricalStatus.NotCompleted,
                HistoricalStatus.PartiallyCompleted,
            ),
        )
    }

    private suspend fun nullifyScheduleDeviationIfNecessary(
        routine: Routine,
        currentDate: LocalDate
    ) {
        if (routine.schedule.periodSeparationEnabled) {
            val currentDatePeriodRange = routine.schedule.getPeriodRange(currentDate)
            currentDatePeriodRange?.let {
                val dateScheduleDeviationIsActualFor =
                    completionHistoryRepository.getLastHistoryEntryDate(routine.id!!)

                val currentDateIsRoutineStartDate = dateScheduleDeviationIsActualFor == null
                if (currentDateIsRoutineStartDate) {
                    check(currentDate == routine.schedule.routineStartDate) {
                        "Looks like there are no entries in the history but at the moment of this " +
                                "insert status operation current date is not routine start date. " +
                                "This should never happen. If it does, check that database values " +
                                "are populated correctly, and the data doesn't get corrupted."
                    }
                }

                if (dateScheduleDeviationIsActualFor !in it && !currentDateIsRoutineStartDate) {
                    routineRepository.setScheduleDeviation(0)
                }
            }
        }
    }
}