package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.computePlanningStatus
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.model.onVacationHistoricalStatuses
import com.rendox.routinetracker.core.model.toStatusEntry
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetRoutineStatusUseCase(
    private val routineRepository: RoutineRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val insertRoutineStatus: InsertRoutineStatusUseCase,
) {
    suspend operator fun invoke(
        routineId: Long, date: LocalDate, today: LocalDate
    ): RoutineStatus? = invoke(routineId, date..date, today).firstOrNull()?.status

    suspend operator fun invoke(
        routineId: Long,
        dates: LocalDateRange,
        today: LocalDate,
    ): List<StatusEntry> {
        val routine: Routine = routineRepository.getRoutineById(routineId)
        prepopulateHistoryWithMissingDates(routine, today)

        val resultingStatusList = mutableListOf<StatusEntry>()

        val completionHistoryPart =
            completionHistoryRepository.getHistoryEntries(routineId, dates)
        resultingStatusList.addAll(completionHistoryPart.map { it.toStatusEntry() })

        if (completionHistoryPart.isEmpty()) {
            resultingStatusList.addAll(computeFutureStatuses(dates, routine))
        } else if (completionHistoryPart.last().date < dates.endInclusive) {
            val startDate = completionHistoryPart.last().date.plusDays(1)
            val endDate = dates.endInclusive
            resultingStatusList.addAll(computeFutureStatuses(startDate..endDate, routine))
        }

        return resultingStatusList
    }

    private suspend fun prepopulateHistoryWithMissingDates(routine: Routine, today: LocalDate) {
        val yesterday = today.minus(DatePeriod(days = 1))
        val lastDateInHistory = completionHistoryRepository.getLastHistoryEntry(routine.id!!)?.date

        val routineEndDate: LocalDate? = routine.schedule.routineEndDate
        if (routineEndDate != null && yesterday > routineEndDate) return

        if (lastDateInHistory == null && routine.schedule.routineStartDate <= yesterday) {
            for (date in routine.schedule.routineStartDate..yesterday) {
                insertRoutineStatus(
                    routineId = routine.id!!,
                    currentDate = date,
                    completedOnCurrentDate = false,
                    today = today,
                )
            }
            return
        }

        if (lastDateInHistory != null && lastDateInHistory < yesterday) {
            for (date in lastDateInHistory.plusDays(1)..yesterday) {
                insertRoutineStatus(
                    routineId = routine.id!!,
                    currentDate = date,
                    completedOnCurrentDate = false,
                    today = today,
                )
            }
            return
        }
    }

    private suspend fun computeFutureStatuses(
        dateRange: LocalDateRange,
        routine: Routine,
    ): List<StatusEntry> {
        val lastVacationStatus = completionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routine.id!!,
            matchingStatuses = onVacationHistoricalStatuses,
        )

        val statusList = mutableListOf<StatusEntry>()
        val lastHistoryEntry = completionHistoryRepository.getLastHistoryEntry(routine.id!!)
        val schedule = routine.schedule
        val lastPeriod: LocalDateRange? = lastHistoryEntry?.let {
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(
                currentDate = it.date,
                lastVacationEndDate = lastVacationStatus?.date,
            )
            else null
        }
        val numOfTimesCompletedInLastPeriodAtTheMomentOfLastHistoryEntryDate =
            lastHistoryEntry?.let {
                completionHistoryRepository.getTotalTimesCompletedInPeriod(
                    routineId = routine.id!!,
                    startDate = lastPeriod?.start ?: routine.schedule.routineStartDate,
                    endDate = it.date,
                )
            } ?: 0.0
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled
        val scheduleDeviationAtTheMomentOfLastHistoryEntryDate =
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = routine.id!!,
                startDate = if (periodSeparationEnabled && lastPeriod != null) {
                    lastPeriod.start
                } else {
                    schedule.routineStartDate
                },
                endDate = dateRange.start,
            )
        val scheduleDeviationInCurrentPeriod = lastPeriod?.let {
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = routine.id!!,
                startDate = it.start,
                endDate = dateRange.start,
            )
        }

        for (date in dateRange) {
            routine.computePlanningStatus(
                validationDate = date,
                currentScheduleDeviation = scheduleDeviationAtTheMomentOfLastHistoryEntryDate,
                actualDate = lastHistoryEntry?.date,
                numOfTimesCompletedInCurrentPeriod = numOfTimesCompletedInLastPeriodAtTheMomentOfLastHistoryEntryDate,
                scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod,
                lastVacationEndDate = lastVacationStatus?.date,
            )?.let {
                statusList.add(StatusEntry(date = date, status = it))
            }
        }
        return statusList
    }
}