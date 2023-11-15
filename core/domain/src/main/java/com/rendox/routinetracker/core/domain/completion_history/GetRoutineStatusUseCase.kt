package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.routine.computePlanningStatus
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.model.StatusEntry
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
        val yesterday = today.minus(DatePeriod(days = 1))
        prepopulateHistoryWithMissingDates(routineId, yesterday)

        val resultingStatusList = mutableListOf<StatusEntry>()

        val completionHistoryPart =
            completionHistoryRepository.getHistoryEntries(routineId, dates)
        resultingStatusList.addAll(completionHistoryPart.map { it.toStatusEntry() })

        val routine: Routine = routineRepository.getRoutineById(routineId)

        if (completionHistoryPart.isEmpty()) {
            resultingStatusList.addAll(computeStatuses(dates, routine))
        } else if (completionHistoryPart.last().date < dates.endInclusive) {
            val startDate = completionHistoryPart.last().date.plusDays(1)
            val endDate = dates.endInclusive
            resultingStatusList.addAll(computeStatuses(startDate..endDate, routine))
        }

        return resultingStatusList
    }

    private suspend fun prepopulateHistoryWithMissingDates(routineId: Long, yesterday: LocalDate) {
        val lastDateInHistory = completionHistoryRepository.getLastHistoryEntry(routineId)?.date
        val routine = routineRepository.getRoutineById(routineId)

        val routineEndDate: LocalDate? = routine.schedule.routineEndDate
        if (routineEndDate != null && yesterday > routineEndDate ) return

        if (lastDateInHistory == null && routine.schedule.routineStartDate <= yesterday) {
            for (date in routine.schedule.routineStartDate..yesterday) {
                insertRoutineStatus(
                    routineId = routineId,
                    currentDate = date,
                    completedOnCurrentDate = false,
                )
            }
            return
        }

        if (lastDateInHistory != null && lastDateInHistory < yesterday) {
            for (date in lastDateInHistory.plusDays(1)..yesterday) {
                insertRoutineStatus(
                    routineId = routineId,
                    currentDate = date,
                    completedOnCurrentDate = false,
                )
            }
            return
        }
    }

    private suspend fun computeStatuses(
        dateRange: LocalDateRange,
        routine: Routine,
    ): List<StatusEntry> {
        val statusList = mutableListOf<StatusEntry>()
        val lastHistoryEntry = completionHistoryRepository.getLastHistoryEntry(routine.id!!)
        for (date in dateRange) {
            routine.computePlanningStatus(
                validationDate = date,
                currentScheduleDeviation = lastHistoryEntry?.currentScheduleDeviation ?: 0,
                dateScheduleDeviationIsActualFor = lastHistoryEntry?.date,
            )?.let {
                statusList.add(StatusEntry(date = date, status = it, ))
            }
        }
        return statusList
    }
}