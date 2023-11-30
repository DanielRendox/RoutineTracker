package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.computePlanningStatus
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.domain.streak.BreakStreakUseCase
import com.rendox.routinetracker.core.domain.streak.ContinueStreakIfEndedUseCase
import com.rendox.routinetracker.core.domain.streak.StartStreakOrJoinStreaksUseCase
import com.rendox.routinetracker.core.domain.streak.DeleteStreakIfStartedUseCase
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.onVacationHistoricalStatuses
import com.rendox.routinetracker.core.model.overCompletedStatuses
import com.rendox.routinetracker.core.model.sortedOutBacklogStatuses
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ToggleHistoricalStatusUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val routineRepository: RoutineRepository,
    private val startStreakOrJoinStreaks: StartStreakOrJoinStreaksUseCase,
    private val breakStreak: BreakStreakUseCase,
    private val deleteStreakIfStarted: DeleteStreakIfStartedUseCase,
    private val continueStreakIfEnded: ContinueStreakIfEndedUseCase
) {
    suspend operator fun invoke(
        routineId: Long,
        currentDate: LocalDate,
        today: LocalDate,
    ) {
        if (currentDate > today) return

        val routine = routineRepository.getRoutineById(routineId)
        val oldEntry =
            completionHistoryRepository.getHistoryEntries(routineId, currentDate..currentDate)
                .first()

        if (currentDate == today) {
            deleteStreakIfStarted(routineId, currentDate)
            continueStreakIfEnded(routineId, currentDate)

            if (oldEntry.status in sortedOutBacklogStatuses) {
                undoSortingOutBacklog(routineId = routineId, currentDate = currentDate)
            }

            completionHistoryRepository.deleteHistoryEntry(routineId, currentDate)
            return
        }

        when (oldEntry.status) {
            HistoricalStatus.Completed -> {
                val wasCompletedLater = completionHistoryRepository.checkIfStatusWasCompletedLater(
                    routineId = routineId,
                    date = currentDate
                )
                if (wasCompletedLater) {
                    val overCompletedEntry =
                        completionHistoryRepository.getFirstHistoryEntryByStatus(
                            routineId = routineId,
                            matchingStatuses = overCompletedStatuses,
                            minDate = currentDate.plusDays(1),
                        )!!
                    val overCompletedEntryNewStatus = when (overCompletedEntry.status) {
                        HistoricalStatus.OverCompleted ->
                            HistoricalStatus.SortedOutBacklog

                        HistoricalStatus.OverCompletedOnVacation ->
                            HistoricalStatus.SortedOutBacklogOnVacation

                        else -> throw IllegalStateException()
                    }
                    completionHistoryRepository.updateHistoryEntryByDate(
                        routineId = routineId,
                        date = overCompletedEntry.date,
                        newStatus = overCompletedEntryNewStatus,
                        newScheduleDeviation = 1F,
                        newTimesCompleted = 1F,
                    )

                    completionHistoryRepository.updateHistoryEntryByDate(
                        routineId = routineId,
                        date = currentDate,
                        newStatus = HistoricalStatus.CompletedLater,
                        newScheduleDeviation = -1F,
                        newTimesCompleted = 0F,
                    )
                } else {
                    when (computeCurrentDatePlanningStatus(routine, currentDate)) {
                        PlanningStatus.Planned -> {
                            breakStreak(
                                routineId = routineId,
                                date = currentDate,
                            )

                            val newScheduleDeviation =
                                if (routine.schedule.backlogEnabled) -1F else 0F
                            completionHistoryRepository.updateHistoryEntryByDate(
                                routineId = routineId,
                                date = currentDate,
                                newStatus = HistoricalStatus.NotCompleted,
                                newScheduleDeviation = newScheduleDeviation,
                                newTimesCompleted = 0F,
                            )
                        }

                        PlanningStatus.AlreadyCompleted -> {
                            completionHistoryRepository.updateHistoryEntryByDate(
                                routineId = routineId,
                                date = currentDate,
                                newStatus = HistoricalStatus.AlreadyCompleted,
                                newScheduleDeviation = -1F,
                                newTimesCompleted = 0F,
                            )
                        }

                        else -> throw IllegalArgumentException()
                    }
                }
            }

            HistoricalStatus.CompletedLater -> {
                val sortedOutBacklogEntry =
                    completionHistoryRepository.getFirstHistoryEntryByStatus(
                        routineId = routineId,
                        matchingStatuses = sortedOutBacklogStatuses,
                        minDate = currentDate.plusDays(1),
                    )!!
                val sortedOutBacklogEntryNewStatus = when (sortedOutBacklogEntry.status) {
                    HistoricalStatus.SortedOutBacklog ->
                        HistoricalStatus.OverCompleted

                    HistoricalStatus.SortedOutBacklogOnVacation ->
                        HistoricalStatus.OverCompletedOnVacation

                    else -> throw IllegalStateException()
                }

                val overCompletedScheduleDeviation =
                    if (routine.schedule.cancelDuenessIfDoneAhead) 1F else 0F
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = sortedOutBacklogEntry.date,
                    newStatus = sortedOutBacklogEntryNewStatus,
                    newScheduleDeviation = overCompletedScheduleDeviation,
                    newTimesCompleted = 1F,
                )

                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )
            }

            HistoricalStatus.NotCompleted -> {
                startStreakOrJoinStreaks(
                    routine = routine,
                    date = currentDate,
                )

                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )
            }

            HistoricalStatus.AlreadyCompleted ->
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )

            HistoricalStatus.Skipped -> {
                when (computeCurrentDatePlanningStatus(routine, currentDate)) {
                    PlanningStatus.Backlog -> {
                        sortOutBacklog(
                            routine = routine,
                            completionHistoryRepository = completionHistoryRepository,
                            startStreakOrJoinStreaks = startStreakOrJoinStreaks,
                            currentDate = currentDate,
                        )
                        completionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = currentDate,
                            newStatus = HistoricalStatus.SortedOutBacklog,
                            newScheduleDeviation = 1F,
                            newTimesCompleted = 1F,
                        )
                    }

                    PlanningStatus.NotDue -> {
                        startStreakOrJoinStreaks(
                            routine = routine,
                            date = currentDate,
                        )

                        val newScheduleDeviation =
                            if (routine.schedule.cancelDuenessIfDoneAhead) 1F else 0F
                        completionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = currentDate,
                            newStatus = HistoricalStatus.OverCompleted,
                            newScheduleDeviation = newScheduleDeviation,
                            newTimesCompleted = 1F,
                        )
                    }

                    else -> throw IllegalArgumentException()
                }
            }

            HistoricalStatus.SortedOutBacklog -> {
                undoSortingOutBacklog(routineId = routineId, currentDate = currentDate)
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
            }

            HistoricalStatus.OverCompleted -> {
                deleteStreakIfStarted(routineId, currentDate)

                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
            }

            HistoricalStatus.NotCompletedOnVacation -> {
                val scheduleDeviation = getCurrentScheduleDeviation(routine, currentDate)
                val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
                    routineId = routine.id!!,
                    matchingStatuses = listOf(HistoricalStatus.NotCompleted),
                    maxDate = currentDate.minus(DatePeriod(days = 1)),
                )
                if (scheduleDeviation < 0 && lastNotCompleted != null) {
                    sortOutBacklog(
                        routine = routine,
                        completionHistoryRepository = completionHistoryRepository,
                        startStreakOrJoinStreaks = startStreakOrJoinStreaks,
                        currentDate = currentDate,
                    )
                    completionHistoryRepository.updateHistoryEntryByDate(
                        routineId = routineId,
                        date = currentDate,
                        newStatus = HistoricalStatus.SortedOutBacklogOnVacation,
                        newScheduleDeviation = 1F,
                        newTimesCompleted = 1F,
                    )
                } else {
                    startStreakOrJoinStreaks(
                        routine = routine,
                        date = currentDate,
                    )

                    val newScheduleDeviation =
                        if (routine.schedule.cancelDuenessIfDoneAhead) 1F else 0F
                    completionHistoryRepository.updateHistoryEntryByDate(
                        routineId = routineId,
                        date = currentDate,
                        newStatus = HistoricalStatus.OverCompletedOnVacation,
                        newScheduleDeviation = newScheduleDeviation,
                        newTimesCompleted = 1F,
                    )
                }
            }

            HistoricalStatus.OverCompletedOnVacation -> {
                deleteStreakIfStarted(routineId, currentDate)

                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.NotCompletedOnVacation,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
            }

            HistoricalStatus.SortedOutBacklogOnVacation -> {
                completionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.NotCompletedOnVacation,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )

                undoSortingOutBacklog(routineId = routineId, currentDate = currentDate)
            }
        }
    }

    private suspend fun computeCurrentDatePlanningStatus(
        routine: Routine, date: LocalDate
    ): PlanningStatus? {
        val lastVacationStatus = completionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routine.id!!,
            matchingStatuses = onVacationHistoricalStatuses,
        )

        val dateBeforeCurrent = date.minus(DatePeriod(days = 1))
        val schedule = routine.schedule
        val periodRange: LocalDateRange? =
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(
                currentDate = date,
                lastVacationEndDate = lastVacationStatus?.date,
            )
            else null
        val numOfTimesCompletedInCurrentPeriod =
            completionHistoryRepository.getTotalTimesCompletedInPeriod(
                routineId = routine.id!!,
                startDate = periodRange?.start ?: schedule.routineStartDate,
                endDate = dateBeforeCurrent,
            )
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled
        val currentScheduleDeviation =
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = routine.id!!,
                startDate = if (periodSeparationEnabled && periodRange != null) {
                    periodRange.start
                } else {
                    schedule.routineStartDate
                },
                endDate = date,
            )
        val scheduleDeviationInCurrentPeriod = periodRange?.let {
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = routine.id!!,
                startDate = it.start,
                endDate = date,
            )
        }

        return routine.computePlanningStatus(
            validationDate = date,
            currentScheduleDeviation = currentScheduleDeviation,
            actualDate = dateBeforeCurrent,
            numOfTimesCompletedInCurrentPeriod = numOfTimesCompletedInCurrentPeriod,
            scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod,
            lastVacationEndDate = lastVacationStatus?.date,
        )
    }

    private suspend fun getCurrentScheduleDeviation(routine: Routine, date: LocalDate): Double {
        val lastVacationStatus = completionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routine.id!!,
            matchingStatuses = onVacationHistoricalStatuses,
        )
        val schedule = routine.schedule
        val periodRange: LocalDateRange? =
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(
                currentDate = date,
                lastVacationEndDate = lastVacationStatus?.date,
            )
            else null
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled
        return completionHistoryRepository.getScheduleDeviationInPeriod(
            routineId = routine.id!!,
            startDate = if (periodSeparationEnabled && periodRange != null) {
                periodRange.start
            } else {
                schedule.routineStartDate
            },
            endDate = date,
        )
    }

    private suspend fun undoSortingOutBacklog(routineId: Long, currentDate: LocalDate) {
        val completedLaterEntry = completionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routineId,
            matchingStatuses = listOf(HistoricalStatus.CompletedLater),
            maxDate = currentDate.minus(DatePeriod(days = 1)),
        )!!

        breakStreak(
            routineId = routineId,
            date = completedLaterEntry.date,
        )

        completionHistoryRepository.updateHistoryEntryByDate(
            routineId = routineId,
            newStatus = HistoricalStatus.NotCompleted,
            date = completedLaterEntry.date,
            newScheduleDeviation = completedLaterEntry.scheduleDeviation,
            newTimesCompleted = completedLaterEntry.timesCompleted,
        )

        completionHistoryRepository.deleteCompletedLaterBackupEntry(
            routineId,
            completedLaterEntry.date
        )
    }
}