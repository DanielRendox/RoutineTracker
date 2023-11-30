package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.computePlanningStatus
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.domain.streak.BreakStreakUseCase
import com.rendox.routinetracker.core.domain.streak.StartStreakOrJoinStreaksUseCase
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.onVacationHistoricalStatuses
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

class InsertRoutineStatusUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val routineRepository: RoutineRepository,
    private val startStreakOrJoinStreaks: StartStreakOrJoinStreaksUseCase,
    private val breakStreak: BreakStreakUseCase,
) {

    suspend operator fun invoke(
        routineId: Long,
        currentDate: LocalDate,
        completedOnCurrentDate: Boolean,
        today: LocalDate,
    ) {
        if (currentDate > today) return

        when (val routine = routineRepository.getRoutineById(routineId)) {
            is Routine.YesNoRoutine -> insertYesNoRoutineStatus(
                routine, currentDate, completedOnCurrentDate
            )
        }
    }

    private suspend fun insertYesNoRoutineStatus(
        routine: Routine.YesNoRoutine,
        currentDate: LocalDate,
        completedOnCurrentDate: Boolean,
    ) {
        val lastVacationStatus = completionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routine.id!!,
            matchingStatuses = onVacationHistoricalStatuses,
        )

        val schedule = routine.schedule
        val currentPeriod: LocalDateRange? =
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(
                currentDate = currentDate, lastVacationEndDate = lastVacationStatus?.date
            )
            else null
        val lastHistoryEntry = completionHistoryRepository.getLastHistoryEntry(routine.id!!)
        val numOfTimesCompletedInCurrentPeriod =
            completionHistoryRepository.getTotalTimesCompletedInPeriod(
                routineId = routine.id!!,
                startDate = currentPeriod?.start ?: routine.schedule.routineStartDate,
                endDate = currentDate,
            )
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled

        val startDate = if (periodSeparationEnabled && currentPeriod != null) {
            currentPeriod.start
        } else {
            schedule.routineStartDate
        }

        val currentScheduleDeviation =
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = routine.id!!,
                startDate = startDate,
                endDate = currentDate,
            )

        val scheduleDeviationInCurrentPeriod = currentPeriod?.let {
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = routine.id!!,
                startDate = it.start,
                endDate = currentDate,
            )
        }

        val planningStatus = routine.computePlanningStatus(
            validationDate = currentDate,
            currentScheduleDeviation = currentScheduleDeviation,
            actualDate = lastHistoryEntry?.date,
            numOfTimesCompletedInCurrentPeriod = numOfTimesCompletedInCurrentPeriod,
            scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod,
            lastVacationEndDate = lastVacationStatus?.date,
        )!!

        val historicalStatusData = when (planningStatus) {
            PlanningStatus.Planned -> deriveHistoricalStatusFromPlannedStatus(
                routine,
                routine.schedule,
                completedOnCurrentDate,
                currentDate,
                routine.schedule.backlogEnabled,
                numOfTimesCompletedInCurrentPeriod,
            )

            PlanningStatus.Backlog -> deriveHistoricalStatusFromBacklogStatus(
                completedOnCurrentDate, routine, currentDate
            )

            PlanningStatus.AlreadyCompleted -> deriveHistoricalStatusFromAlreadyCompletedStatus(
                completedOnCurrentDate
            )

            PlanningStatus.NotDue -> deriveHistoricalStatusFromNotDueStatus(
                routine,
                currentDate,
                completedOnCurrentDate,
                routine.schedule.cancelDuenessIfDoneAhead,
            )

            PlanningStatus.OnVacation ->
                deriveHistoricalStatusFromOnVacationStatus(
                    completed = completedOnCurrentDate,
                    routine = routine,
                    currentScheduleDeviation = currentScheduleDeviation,
                    cancelDuenessIfDoneAhead = routine.schedule.cancelDuenessIfDoneAhead,
                    currentDate = currentDate,
                )
        }

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

    private suspend fun deriveHistoricalStatusFromPlannedStatus(
        routine: Routine,
        schedule: Schedule,
        completed: Boolean,
        currentDate: LocalDate,
        backlogEnabled: Boolean,
        timesCompletedInCurrentPeriod: Double,
    ): HistoricalStatusData = if (completed) {
        startStreakOrJoinStreaks(
            routine = routine,
            date = currentDate,
        )
        HistoricalStatusData(
            scheduleDeviation = 0F,
            historicalStatus = HistoricalStatus.Completed,
        )
    } else {
        if (schedule is Schedule.ByNumOfDueDays) {
            val lastVacationStatus = completionHistoryRepository.getLastHistoryEntryByStatus(
                routineId = routine.id!!,
                matchingStatuses = onVacationHistoricalStatuses,
            )

            val validationDatePeriod = (schedule as Schedule.PeriodicSchedule).getPeriodRange(
                currentDate = currentDate,
                lastVacationEndDate = lastVacationStatus?.date,
            )

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
                breakStreak(
                    routineId = routine.id!!,
                    date = currentDate,
                )
                HistoricalStatusData(
                    scheduleDeviation = if (backlogEnabled) -1F else 0F,
                    historicalStatus = HistoricalStatus.NotCompleted,
                )
            }
        } else {
            breakStreak(
                routineId = routine.id!!,
                date = currentDate,
            )
            HistoricalStatusData(
                scheduleDeviation = if (backlogEnabled) -1F else 0F,
                historicalStatus = HistoricalStatus.NotCompleted,
            )
        }
    }

    private suspend fun deriveHistoricalStatusFromBacklogStatus(
        completed: Boolean, routineId: Routine, currentDate: LocalDate
    ): HistoricalStatusData = if (completed) {
        sortOutBacklog(
            routine = routineId,
            completionHistoryRepository = completionHistoryRepository,
            startStreakOrJoinStreaks = startStreakOrJoinStreaks,
            currentDate = currentDate,
        )
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

    private suspend fun deriveHistoricalStatusFromNotDueStatus(
        routine: Routine,
        currentDate: LocalDate,
        completed: Boolean,
        cancelDuenessIfDoneAhead: Boolean,
    ): HistoricalStatusData = if (completed) {
        startStreakOrJoinStreaks(
            routine = routine,
            date = currentDate,
        )
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
        routine: Routine,
        currentScheduleDeviation: Double,
        cancelDuenessIfDoneAhead: Boolean,
        currentDate: LocalDate,
    ): HistoricalStatusData = if (completed) {
        if (currentScheduleDeviation < 0) {
            sortOutBacklog(
                routine = routine,
                completionHistoryRepository = completionHistoryRepository,
                startStreakOrJoinStreaks = startStreakOrJoinStreaks,
                currentDate = currentDate,
            )
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

//    private suspend fun startStreak(routine: Routine, currentDate: LocalDate) {
//        val currentStreakIsStillLasting =
//            streakRepository.getLastStreak(routine.id!!)?.let { it.end == null } ?: false
//        if (!currentStreakIsStillLasting) {
//            val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
//                routineId = routine.id!!,
//                matchingStatuses = listOf(HistoricalStatus.NotCompleted),
//            )
//            val streakStart =
//                if (lastNotCompleted == null) routine.schedule.routineStartDate else currentDate
//
//            streakRepository.insertStreak(routine.id!!, start = streakStart, end = null)
//        }
//    }

//    private suspend fun endStreak(routineId: Long, currentDate: LocalDate) {
//        val existingStreak: Streak? = streakRepository.getLastStreak(routineId)?.let {
//            if (it.end == null) it else null
//        }
//        existingStreak?.let {
//            streakRepository.updateStreakById(
//                id = it.id!!,
//                start = it.start,
//                end = currentDate.minus(DatePeriod(days = 1)),
//            )
//        }
//    }
}