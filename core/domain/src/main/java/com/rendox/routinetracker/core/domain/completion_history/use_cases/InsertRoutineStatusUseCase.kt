package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.computePlanningStatus
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.domain.completion_history.sortOutBacklog
import com.rendox.routinetracker.core.logic.time.LocalDateRange
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
//    private val streakRepository: StreakRepository,
) {

    suspend operator fun invoke(
        routineId: Long,
        currentDate: LocalDate,
        completedOnCurrentDate: Boolean,
    ) = when (val routine = routineRepository.getRoutineById(routineId)) {
        is Routine.YesNoRoutine -> insertYesNoRoutineStatus(
            routine, currentDate, completedOnCurrentDate
        )
    }

    private suspend fun insertYesNoRoutineStatus(
        routine: Routine.YesNoRoutine,
        currentDate: LocalDate,
        completedOnCurrentDate: Boolean,
    ) {
        val schedule = routine.schedule
        val periodRange: LocalDateRange? =
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(currentDate)
            else null
        val lastHistoryEntry = completionHistoryRepository.getLastHistoryEntry(routine.id!!)
        val numOfTimesCompletedInCurrentPeriod =
            completionHistoryRepository.getTotalTimesCompletedInPeriod(
                routineId = routine.id!!,
                startDate = periodRange?.start ?: routine.schedule.routineStartDate,
                endDate = currentDate,
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
                endDate = currentDate,
            )
        val planningStatus = routine.computePlanningStatus(
            validationDate = currentDate,
            currentScheduleDeviation = currentScheduleDeviation,
            actualDate = lastHistoryEntry?.date,
            numOfTimesCompletedInCurrentPeriod = numOfTimesCompletedInCurrentPeriod,
        )!!
        println("$currentDate (${currentDate.dayOfWeek}): planningStatus = $planningStatus")

        val historicalStatusData = when (planningStatus) {
            PlanningStatus.Planned -> deriveHistoricalStatusFromPlannedStatus(
                routine.schedule,
                completedOnCurrentDate,
                currentDate,
                routine.schedule.backlogEnabled,
                numOfTimesCompletedInCurrentPeriod,
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
                    completed = completedOnCurrentDate,
                    routineId = routine.id!!,
                    currentScheduleDeviation = currentScheduleDeviation,
                    cancelDuenessIfDoneAhead = routine.schedule.cancelDuenessIfDoneAhead,
                )
        }

//        val currentStreak: Streak? = streakRepository.getStreakByDate(
//            routineId = routine.id!!,
//            dateWithinStreak = currentDate,
//        )
//
//        when (historicalStatusData.historicalStatus) {
//            HistoricalStatus.NotCompleted -> if (currentStreak != null) {
//                val newValue = currentStreak.copy(end = currentDate.minus(DatePeriod(days = 1)))
//                streakRepository.updateStreakById(currentStreak.id!!, newValue)
//            }
//
//            HistoricalStatus.Completed,
//            HistoricalStatus.OverCompleted,
//            HistoricalStatus.OverCompletedOnVacation -> if (currentStreak == null) {
//                streakRepository.insertStreak(
//                    routineId = routine.id!!,
//                    streak = Streak(start = currentDate, end = null),
//                )
//            }
//
//            HistoricalStatus.SortedOutBacklog,
//            HistoricalStatus.SortedOutBacklogOnVacation -> if
//        }

        completionHistoryRepository.insertHistoryEntry(
            routineId = routine.id!!,
            entry = CompletionHistoryEntry(
                date = currentDate,
                status = historicalStatusData.historicalStatus,
                scheduleDeviation = historicalStatusData.scheduleDeviation,
                timesCompleted = if (completedOnCurrentDate) 1F else 0F,
            ),
        )
    }

    private fun deriveHistoricalStatusFromPlannedStatus(
        schedule: Schedule,
        completed: Boolean,
        currentDate: LocalDate,
        backlogEnabled: Boolean,
        timesCompletedInCurrentPeriod: Double,
    ): HistoricalStatusData = if (completed) {
        HistoricalStatusData(
            scheduleDeviation = 0F,
            historicalStatus = HistoricalStatus.Completed,
        )
    } else {
        if (schedule is Schedule.ByNumOfDueDays) {
            val validationDatePeriod =
                (schedule as Schedule.PeriodicSchedule).getPeriodRange(currentDate)

            val numOfDueDays =
                schedule.getNumOfDueDatesInPeriod(validationDatePeriod)

            val numOfDaysThatRemainToBeCompletedInPeriod =
                numOfDueDays - timesCompletedInCurrentPeriod
            val daysRemainingInCurrentPeriod =
                currentDate.daysUntil(validationDatePeriod.endInclusive) + 1 // including today
            val thereAreEnoughDaysInPeriodToCompleteLater =
                numOfDaysThatRemainToBeCompletedInPeriod < (daysRemainingInCurrentPeriod.toDouble())

            if (thereAreEnoughDaysInPeriodToCompleteLater) {
                HistoricalStatusData(
                    scheduleDeviation = 0F,
                    historicalStatus = HistoricalStatus.Skipped,
                )
            } else {
                HistoricalStatusData(
                    scheduleDeviation = if (backlogEnabled) -1F else 0F,
                    historicalStatus = HistoricalStatus.NotCompleted,
                )
            }
        } else {
            HistoricalStatusData(
                scheduleDeviation = if (backlogEnabled) -1F else 0F,
                historicalStatus = HistoricalStatus.NotCompleted,
            )
        }
    }

    private suspend fun deriveHistoricalStatusFromBacklogStatus(
        completed: Boolean, routineId: Long
    ): HistoricalStatusData = if (completed) {
        sortOutBacklog(routineId, completionHistoryRepository)

        HistoricalStatusData(
            scheduleDeviation = 1F,
            historicalStatus = HistoricalStatus.SortedOutBacklog,
        )
    } else {
        HistoricalStatusData(
            scheduleDeviation = 0F,
            historicalStatus = HistoricalStatus.Skipped,
        )
    }

    private fun deriveHistoricalStatusFromAlreadyCompletedStatus(
        completed: Boolean
    ): HistoricalStatusData = if (completed) {
        HistoricalStatusData(
            scheduleDeviation = 0F,
            historicalStatus = HistoricalStatus.Completed
        )
    } else {
        HistoricalStatusData(
            scheduleDeviation = -1F,
            historicalStatus = HistoricalStatus.AlreadyCompleted
        )
    }

    private fun deriveHistoricalStatusFromNotDueStatus(
        completed: Boolean, cancelDuenessIfDoneAhead: Boolean
    ): HistoricalStatusData = if (completed) {
        HistoricalStatusData(
            scheduleDeviation = if (cancelDuenessIfDoneAhead) 1F else 0F,
            historicalStatus = HistoricalStatus.OverCompleted,
        )
    } else {
        HistoricalStatusData(
            scheduleDeviation = 0F,
            historicalStatus = HistoricalStatus.Skipped,
        )
    }

    private suspend fun deriveHistoricalStatusFromOnVacationStatus(
        completed: Boolean,
        routineId: Long,
        currentScheduleDeviation: Double,
        cancelDuenessIfDoneAhead: Boolean,
    ): HistoricalStatusData = if (completed) {
        if (currentScheduleDeviation < 0) {
            sortOutBacklog(routineId, completionHistoryRepository)
            HistoricalStatusData(
                scheduleDeviation = 1F,
                historicalStatus = HistoricalStatus.SortedOutBacklogOnVacation,
            )
        } else {
            HistoricalStatusData(
                scheduleDeviation = if (cancelDuenessIfDoneAhead) 1F else 0F,
                historicalStatus = HistoricalStatus.OverCompletedOnVacation,
            )
        }
    } else {
        HistoricalStatusData(
            scheduleDeviation = 0F,
            historicalStatus = HistoricalStatus.NotCompletedOnVacation,
        )
    }

    private data class HistoricalStatusData(
        val scheduleDeviation: Float,
        val historicalStatus: HistoricalStatus,
    )
}