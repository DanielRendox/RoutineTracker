package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.computePlanningStatus
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.domain.completion_history.sortOutBacklog
import com.rendox.routinetracker.core.domain.completion_history.undoSortingOutBacklog
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ToggleHistoricalStatusUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val routineRepository: RoutineRepository,
) {
    suspend operator fun invoke(
        routineId: Long,
        date: LocalDate,
        today: LocalDate,
    ) {
        if (date >= today) {
            completionHistoryRepository.deleteHistoryEntry(routineId, date)
            return
        }

        val routine = routineRepository.getRoutineById(routineId)

        val oldEntry =
            completionHistoryRepository.getHistoryEntries(routineId, date..date).first()
        when (oldEntry.status) {
            HistoricalStatus.Completed -> {
                val wasCompletedLater =
                    completionHistoryRepository.checkIfStatusWasCompletedLater(routineId, date)
                if (wasCompletedLater) {
                    completionHistoryRepository.updateHistoryEntryByDate(
                        routineId = routineId,
                        date = date,
                        newStatus = HistoricalStatus.CompletedLater,
                        newScheduleDeviation = -1F,
                        newTimesCompleted = 0F,
                    )
                } else {
                    when (computeCurrentDatePlanningStatus(routineId, date)) {
                        PlanningStatus.Planned -> {
                            val newScheduleDeviation =
                                if (routine.schedule.backlogEnabled) -1F else 0F
                            completionHistoryRepository.updateHistoryEntryByDate(
                                routineId = routineId,
                                date = date,
                                newStatus = HistoricalStatus.NotCompleted,
                                newScheduleDeviation = newScheduleDeviation,
                                newTimesCompleted = 0F,
                            )
                        }

                        PlanningStatus.AlreadyCompleted -> {
                            completionHistoryRepository.updateHistoryEntryByDate(
                                routineId = routineId,
                                date = date,
                                newStatus = HistoricalStatus.AlreadyCompleted,
                                newScheduleDeviation = 0F,
                                newTimesCompleted = 0F,
                            )
                        }

                        else -> throw IllegalArgumentException()
                    }
                }
            }

            HistoricalStatus.CompletedLater ->
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )

            HistoricalStatus.NotCompleted ->
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )

            HistoricalStatus.AlreadyCompleted ->
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )

            HistoricalStatus.Skipped -> {
                when (computeCurrentDatePlanningStatus(routineId, date)) {
                    PlanningStatus.Backlog -> {
                        sortOutBacklog(routineId, completionHistoryRepository)
                        completionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = date,
                            newStatus = HistoricalStatus.SortedOutBacklog,
                            newScheduleDeviation = 1F,
                            newTimesCompleted = 1F,
                        )
                    }

                    PlanningStatus.NotDue -> {
                        val newScheduleDeviation =
                            if (routine.schedule.cancelDuenessIfDoneAhead) 1F else 0F
                        completionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = date,
                            newStatus = HistoricalStatus.OverCompleted,
                            newScheduleDeviation = newScheduleDeviation,
                            newTimesCompleted = 1F,
                        )
                    }

                    else -> throw IllegalArgumentException()
                }
            }

            HistoricalStatus.SortedOutBacklog -> {
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
                undoSortingOutBacklog(routineId, completionHistoryRepository)
            }

            HistoricalStatus.OverCompleted ->
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )

            HistoricalStatus.NotCompletedOnVacation -> {
                when (computeCurrentDatePlanningStatus(routineId, date)) {
                    PlanningStatus.Backlog -> {
                        sortOutBacklog(routineId, completionHistoryRepository)
                        completionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = date,
                            newStatus = HistoricalStatus.SortedOutBacklogOnVacation,
                            newScheduleDeviation = 1F,
                            newTimesCompleted = 1F,
                        )
                    }

                    PlanningStatus.OnVacation -> {
                        val newScheduleDeviation =
                            if (routine.schedule.cancelDuenessIfDoneAhead) 1F else 0F
                        completionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = date,
                            newStatus = HistoricalStatus.OverCompletedOnVacation,
                            newScheduleDeviation = newScheduleDeviation,
                            newTimesCompleted = 1F,
                        )
                    }
                    else -> throw IllegalArgumentException()
                }
            }

            HistoricalStatus.OverCompletedOnVacation ->
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.NotCompletedOnVacation,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )

            HistoricalStatus.SortedOutBacklogOnVacation -> {
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.NotCompletedOnVacation,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
                undoSortingOutBacklog(routineId, completionHistoryRepository)
            }
        }
    }

    private suspend fun computeCurrentDatePlanningStatus(
        routineId: Long, date: LocalDate
    ): PlanningStatus? {
        val dateBeforeCurrent = date.minus(DatePeriod(days = 1))
        val routine = routineRepository.getRoutineById(routineId)
        val schedule = routine.schedule
        val periodRange: LocalDateRange? =
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(date)
            else null
        val numOfTimesCompletedInCurrentPeriod =
            completionHistoryRepository.getTotalTimesCompletedInPeriod(
                routineId = routineId,
                startDate = periodRange?.start ?: schedule.routineStartDate,
                endDate = dateBeforeCurrent,
            )
        val currentScheduleDeviation =
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = routineId,
                startDate = schedule.routineStartDate,
                endDate = date,
            )
        return routine.computePlanningStatus(
            validationDate = date,
            currentScheduleDeviation = currentScheduleDeviation,
            actualDate = dateBeforeCurrent,
            numOfTimesCompletedInCurrentPeriod = numOfTimesCompletedInCurrentPeriod,
        )
    }
}