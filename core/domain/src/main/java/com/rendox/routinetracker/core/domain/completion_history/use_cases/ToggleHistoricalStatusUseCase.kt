package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.routine_completion_history.RoutineCompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.HabitRepository
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.domain.streak.BreakStreakUseCase
import com.rendox.routinetracker.core.domain.streak.ContinueStreakIfEndedUseCase
import com.rendox.routinetracker.core.domain.streak.StartStreakOrJoinStreaksUseCase
import com.rendox.routinetracker.core.domain.streak.DeleteStreakIfStartedUseCase
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.onVacationHistoricalStatuses
import com.rendox.routinetracker.core.model.overCompletedStatuses
import com.rendox.routinetracker.core.model.sortedOutBacklogStatuses
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ToggleHistoricalStatusUseCase(
    private val routineCompletionHistoryRepository: RoutineCompletionHistoryRepository,
    private val habitRepository: HabitRepository,
    private val startStreakOrJoinStreaks: StartStreakOrJoinStreaksUseCase,
    private val breakStreak: BreakStreakUseCase,
    private val deleteStreakIfStarted: DeleteStreakIfStartedUseCase,
    private val continueStreakIfEnded: ContinueStreakIfEndedUseCase
) {
    private var currentPeriod: LocalDateRange? = null
    private var lastVacationStatus: CompletionHistoryEntry? = null

    suspend operator fun invoke(
        routineId: Long,
        currentDate: LocalDate,
        today: LocalDate,
    ) {
        if (currentDate > today) return

        val routine = habitRepository.getHabitById(routineId)
        val oldEntry =
            routineCompletionHistoryRepository.getHistoryEntries(routineId, currentDate..currentDate)
                .first()

        if (currentDate == today) {
            deleteStreakIfStarted(routineId, currentDate)
            continueStreakIfEnded(routineId, currentDate)

            if (oldEntry.status in sortedOutBacklogStatuses) {
                undoSortingOutBacklog(routineId = routineId, currentDate = currentDate)
            }

            routineCompletionHistoryRepository.deleteHistoryEntry(routineId, currentDate)
            return
        }

        lastVacationStatus = routineCompletionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routine.id!!,
            matchingStatuses = onVacationHistoricalStatuses,
        )

        val schedule = routine.schedule
        currentPeriod =
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(
                currentDate = currentDate,
                lastVacationEndDate = lastVacationStatus?.date,
            )
            else null

        println("ToggleHistoricalStatusUseCase oldStatus = ${oldEntry.status}")
        when (oldEntry.status) {
            HistoricalStatus.Completed -> {
                val wasCompletedLater = routineCompletionHistoryRepository.checkIfStatusWasCompletedLater(
                    routineId = routineId,
                    date = currentDate
                )
                if (wasCompletedLater) {
                    val overCompletedEntry =
                        routineCompletionHistoryRepository.getFirstHistoryEntryByStatus(
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
                    routineCompletionHistoryRepository.updateHistoryEntryByDate(
                        routineId = routineId,
                        date = overCompletedEntry.date,
                        newStatus = overCompletedEntryNewStatus,
                        newScheduleDeviation = 1F,
                        newTimesCompleted = 1F,
                    )

                    routineCompletionHistoryRepository.updateHistoryEntryByDate(
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
                            routineCompletionHistoryRepository.updateHistoryEntryByDate(
                                routineId = routineId,
                                date = currentDate,
                                newStatus = HistoricalStatus.NotCompleted,
                                newScheduleDeviation = newScheduleDeviation,
                                newTimesCompleted = 0F,
                            )
                        }

                        PlanningStatus.AlreadyCompleted -> {
                            routineCompletionHistoryRepository.updateHistoryEntryByDate(
                                routineId = routineId,
                                date = currentDate,
                                newStatus = HistoricalStatus.AlreadyCompleted,
                                newScheduleDeviation = -1F,
                                newTimesCompleted = 0F,
                            )
                            undoCompletingAhead(routine, currentDate)
                        }

                        else -> throw IllegalArgumentException()
                    }
                }
            }

            HistoricalStatus.CompletedLater -> {
                val sortedOutBacklogEntry =
                    routineCompletionHistoryRepository.getFirstHistoryEntryByStatus(
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
                    if (routine.schedule.completingAheadEnabled) 1F else 0F
                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = sortedOutBacklogEntry.date,
                    newStatus = sortedOutBacklogEntryNewStatus,
                    newScheduleDeviation = overCompletedScheduleDeviation,
                    newTimesCompleted = 1F,
                )

                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )
            }

            HistoricalStatus.NotCompleted -> {
                startStreakOrJoinStreaks(
                    habit = routine,
                    date = currentDate,
                )

                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )
            }

            HistoricalStatus.AlreadyCompleted -> {
                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Completed,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 1F,
                )
                completeAhead(routine, currentDate)
            }

            HistoricalStatus.Skipped -> {
                when (computeCurrentDatePlanningStatus(routine, currentDate)) {
                    PlanningStatus.Backlog -> {
                        sortOutBacklog(
                            habit = routine,
                            routineCompletionHistoryRepository = routineCompletionHistoryRepository,
                            startStreakOrJoinStreaks = startStreakOrJoinStreaks,
                            currentDate = currentDate,
                        )
                        routineCompletionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = currentDate,
                            newStatus = HistoricalStatus.SortedOutBacklog,
                            newScheduleDeviation = 1F,
                            newTimesCompleted = 1F,
                        )
                    }


                    PlanningStatus.NotDue -> {
                        val newScheduleDeviation =
                            if (routine.schedule.completingAheadEnabled) 1F else 0F
                        routineCompletionHistoryRepository.updateHistoryEntryByDate(
                            routineId = routineId,
                            date = currentDate,
                            newStatus = HistoricalStatus.OverCompleted,
                            newScheduleDeviation = newScheduleDeviation,
                            newTimesCompleted = 1F,
                        )

                        println("ToggleHistoricalStatusUseCase newScheduleDeviation = $newScheduleDeviation")
                        completeAhead(
                            routine, currentDate
                        )

                        startStreakOrJoinStreaks(
                            habit = routine,
                            date = currentDate,
                        )
                    }

                    else -> throw IllegalArgumentException()
                }
            }

            HistoricalStatus.SortedOutBacklog -> {
                undoSortingOutBacklog(routineId = routineId, currentDate = currentDate)
                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
            }

            HistoricalStatus.OverCompleted -> {
                deleteStreakIfStarted(routineId, currentDate)

                undoCompletingAhead(routine, currentDate)
                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.Skipped,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
            }

            HistoricalStatus.NotCompletedOnVacation -> {
                val scheduleDeviation = getCurrentScheduleDeviation(routine, currentDate)
                val lastNotCompleted = routineCompletionHistoryRepository.getLastHistoryEntryByStatus(
                    routineId = routine.id!!,
                    matchingStatuses = listOf(HistoricalStatus.NotCompleted),
                    maxDate = currentDate.minus(DatePeriod(days = 1)),
                )
                if (scheduleDeviation < 0 && lastNotCompleted != null) {
                    sortOutBacklog(
                        habit = routine,
                        routineCompletionHistoryRepository = routineCompletionHistoryRepository,
                        startStreakOrJoinStreaks = startStreakOrJoinStreaks,
                        currentDate = currentDate,
                    )
                    routineCompletionHistoryRepository.updateHistoryEntryByDate(
                        routineId = routineId,
                        date = currentDate,
                        newStatus = HistoricalStatus.SortedOutBacklogOnVacation,
                        newScheduleDeviation = 1F,
                        newTimesCompleted = 1F,
                    )
                } else {
                    startStreakOrJoinStreaks(
                        habit = routine,
                        date = currentDate,
                    )

                    val newScheduleDeviation =
                        if (routine.schedule.completingAheadEnabled) 1F else 0F
                    routineCompletionHistoryRepository.updateHistoryEntryByDate(
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

                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = routineId,
                    date = currentDate,
                    newStatus = HistoricalStatus.NotCompletedOnVacation,
                    newScheduleDeviation = 0F,
                    newTimesCompleted = 0F,
                )
            }

            HistoricalStatus.SortedOutBacklogOnVacation -> {
                routineCompletionHistoryRepository.updateHistoryEntryByDate(
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
        habit: Habit, date: LocalDate
    ): PlanningStatus? {
        val dateBeforeCurrent = date.minus(DatePeriod(days = 1))
        val schedule = habit.schedule
        val numOfTimesCompletedInCurrentPeriod =
            routineCompletionHistoryRepository.getTotalTimesCompletedInPeriod(
                routineId = habit.id!!,
                startDate = currentPeriod?.start ?: schedule.startDate,
                endDate = dateBeforeCurrent,
            )
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled
        val periodRange = currentPeriod
        val startDate = if (periodSeparationEnabled && periodRange != null) {
            periodRange.start
        } else {
            schedule.startDate
        }
        val currentScheduleDeviation =
            routineCompletionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = habit.id!!,
                startDate = startDate,
                endDate = date,
            )
        val scheduleDeviationInCurrentPeriod = periodRange?.let {
            routineCompletionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = habit.id!!,
                startDate = it.start,
                endDate = date,
            )
        }
        println("period = $startDate..$date")
        println("currentScheduleDeviation = $currentScheduleDeviation")

//        return habit.computePlanningStatus(
//            validationDate = date,
//            currentScheduleDeviation = currentScheduleDeviation,
//            actualDate = dateBeforeCurrent,
//            numOfTimesCompletedInCurrentPeriod = numOfTimesCompletedInCurrentPeriod,
//            scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod,
//            lastVacationEndDate = lastVacationStatus?.date,
//        )
        TODO()
    }

    private suspend fun getCurrentScheduleDeviation(habit: Habit, date: LocalDate): Double {
        val schedule = habit.schedule
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled
        val periodRange = currentPeriod
        return routineCompletionHistoryRepository.getScheduleDeviationInPeriod(
            routineId = habit.id!!,
            startDate = if (periodSeparationEnabled && periodRange != null) {
                periodRange.start
            } else {
                schedule.startDate
            },
            endDate = date,
        )
    }

    private suspend fun undoSortingOutBacklog(routineId: Long, currentDate: LocalDate) {
        val completedLaterEntry = routineCompletionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routineId,
            matchingStatuses = listOf(HistoricalStatus.CompletedLater),
            maxDate = currentDate.minus(DatePeriod(days = 1)),
        )!!

        breakStreak(
            routineId = routineId,
            date = completedLaterEntry.date,
        )

        routineCompletionHistoryRepository.updateHistoryEntryByDate(
            routineId = routineId,
            newStatus = HistoricalStatus.NotCompleted,
            date = completedLaterEntry.date,
            newScheduleDeviation = completedLaterEntry.scheduleDeviation,
            newTimesCompleted = completedLaterEntry.timesCompleted,
        )

        routineCompletionHistoryRepository.deleteCompletedLaterBackupEntry(
            routineId,
            completedLaterEntry.date
        )
    }

    private suspend fun completeAhead(
        habit: Habit,
        currentDate: LocalDate,
    ) {
        val schedule = habit.schedule
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled

        println("ToggleHistoricalStatusUseCase periodSeparationEnabled = $periodSeparationEnabled")
        println("ToggleHistoricalStatusUseCase currentPeriod = $currentPeriod")

        if (schedule.completingAheadEnabled) {
            val nextNotCompletedEntry =
                routineCompletionHistoryRepository.getFirstHistoryEntryByStatus(
                    routineId = habit.id!!,
                    matchingStatuses = listOf(HistoricalStatus.NotCompleted),
                    minDate = currentDate.plusDays(1),
                )
            if (nextNotCompletedEntry != null &&
                (!periodSeparationEnabled
                        || nextNotCompletedEntry.date in currentPeriod!!)
            ) {
                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = habit.id!!,
                    date = nextNotCompletedEntry.date,
                    newStatus = HistoricalStatus.AlreadyCompleted,
                    newScheduleDeviation = -1F,
                    newTimesCompleted = 0F,
                )
            }
        }
    }

    private suspend fun undoCompletingAhead(
        habit: Habit,
        currentDate: LocalDate,
    ) {
        val schedule = habit.schedule
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled

        if (schedule.completingAheadEnabled) {
            val lastCompletedAheadEntry =
                routineCompletionHistoryRepository.getLastHistoryEntryByStatus(
                    routineId = habit.id!!,
                    matchingStatuses = listOf(HistoricalStatus.AlreadyCompleted),
                    minDate = currentDate.plusDays(1),
                )
            if (lastCompletedAheadEntry != null &&
                (!periodSeparationEnabled
                        || lastCompletedAheadEntry.date in currentPeriod!!)
            ) {
                val newScheduleDeviation =
                    if (habit.schedule.backlogEnabled) -1F else 0F
                routineCompletionHistoryRepository.updateHistoryEntryByDate(
                    routineId = habit.id!!,
                    date = lastCompletedAheadEntry.date,
                    newStatus = HistoricalStatus.NotCompleted,
                    newScheduleDeviation = newScheduleDeviation,
                    newTimesCompleted = 0F,
                )
            }
        }
    }
}