package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.routine.computePlanningStatus
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.RoutineStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ToggleRoutineStatusUseCase(
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
            println("ToggleRoutineStatusUseCase: history entry deleted ($routineId, $date)")
            return
        }
        
//        val oldValue = completionHistoryRepository.
        
        when (completionHistoryRepository.getHistoryEntries(routineId, date..date).first().status) {
            HistoricalStatus.Completed -> {
                val newHistoricalStatus =
                    if (completionHistoryRepository.checkIfStatusWasCompletedLater(
                            routineId,
                            date
                        )
                    ) {
                        HistoricalStatus.CompletedLater
                    } else {
                        val dateBeforeCurrent = date.minus(DatePeriod(days = 1))
                        val planningStatus =
                            routineRepository.getRoutineById(routineId).computePlanningStatus(
                                validationDate = date,
                                currentScheduleDeviation = completionHistoryRepository.getHistoryEntries(
                                    routineId = routineId,
                                    dates = dateBeforeCurrent..dateBeforeCurrent,
                                ).firstOrNull()?.currentScheduleDeviation ?: 0,
                                dateScheduleDeviationIsActualFor = dateBeforeCurrent,
                            )

                        when (planningStatus) {
                            PlanningStatus.Planned -> HistoricalStatus.NotCompleted
                            PlanningStatus.AlreadyCompleted -> HistoricalStatus.Skipped
                            else -> throw IllegalArgumentException()
                        }
                    }

                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = newHistoricalStatus,
                    newScheduleDeviation = -1,
                )
            }

            HistoricalStatus.CompletedLater ->
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 1,
                )

            HistoricalStatus.NotCompleted ->
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 1,
                )

            HistoricalStatus.AlreadyCompleted ->
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0,
                )

            HistoricalStatus.Skipped -> {
                val dateBeforeCurrent = date.minus(DatePeriod(days = 1))
                val newStatus: HistoricalStatus =
                    when (
                        routineRepository.getRoutineById(routineId).computePlanningStatus(
                            validationDate = date,
                            currentScheduleDeviation = completionHistoryRepository.getHistoryEntries(
                                routineId = routineId,
                                dates = dateBeforeCurrent..dateBeforeCurrent,
                            ).firstOrNull()?.currentScheduleDeviation ?: 0,
                            dateScheduleDeviationIsActualFor = dateBeforeCurrent,
                        )
                    ) {
                        PlanningStatus.Backlog -> {
                            sortOutBacklog(routineId, completionHistoryRepository)
                            HistoricalStatus.SortedOutBacklog
                        }

                        PlanningStatus.NotDue -> HistoricalStatus.OverCompleted
                        else -> throw IllegalArgumentException()
                    }
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = newStatus,
                    newScheduleDeviation = 1,
                )
            }

            HistoricalStatus.SortedOutBacklog -> {
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = -1,
                )
                undoSortingOutBacklog(routineId, completionHistoryRepository)
            }

            HistoricalStatus.OverCompleted ->
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = -1,
                )

            HistoricalStatus.NotCompletedOnVacation -> {
                val dateBeforeCurrent = date.minus(DatePeriod(days = 1))
                val newStatus: HistoricalStatus =
                    when (
                        routineRepository.getRoutineById(routineId).computePlanningStatus(
                            validationDate = date,
                            currentScheduleDeviation = completionHistoryRepository.getHistoryEntries(
                                routineId = routineId,
                                dates = dateBeforeCurrent..dateBeforeCurrent,
                            ).firstOrNull()?.currentScheduleDeviation ?: 0,
                            dateScheduleDeviationIsActualFor = dateBeforeCurrent,
                        )
                    ) {
                        PlanningStatus.Backlog -> {
                            sortOutBacklog(routineId, completionHistoryRepository)
                            HistoricalStatus.SortedOutBacklogOnVacation
                        }

                        PlanningStatus.OnVacation -> HistoricalStatus.OverCompletedOnVacation
                        else -> throw IllegalArgumentException()
                    }
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = newStatus,
                    newScheduleDeviation = 1,
                )
            }

            HistoricalStatus.OverCompletedOnVacation ->
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.NotCompletedOnVacation,
                    newScheduleDeviation = -1,
                )

            HistoricalStatus.SortedOutBacklogOnVacation -> {
                completionHistoryRepository.updateHistoryEntryStatusByDate(
                    routineId = routineId,
                    date = date,
                    newStatus = HistoricalStatus.NotCompletedOnVacation,
                    newScheduleDeviation = -1,
                )
                undoSortingOutBacklog(routineId, completionHistoryRepository)
            }
        }
    }
}