package com.rendox.routinetracker.core.domain.routine

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.model.toStatusEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class GetRoutineStatusUseCase(
    private val routineRepository: RoutineRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val insertRoutineStatusIntoHistory: InsertRoutineStatusIntoHistoryUseCase,
) {
    suspend operator fun invoke(
        routineId: Long,
        dates: LocalDateRange,
    ): List<StatusEntry?> {
        prepopulateHistoryWithMissingDates(routineId)

        val resultingStatusList = mutableListOf<StatusEntry?>()

        val completionHistoryPart =
            completionHistoryRepository.getHistoryEntries(routineId, dates)
        resultingStatusList.addAll(completionHistoryPart.map { it.toStatusEntry() })

        if (completionHistoryPart.last().date < dates.endInclusive) {
            val startDate = completionHistoryPart.last().date.plusDays(1)
            val endDate = dates.endInclusive
            resultingStatusList.addAll(computeFutureDates(startDate..endDate, routineId))
        }

        return resultingStatusList
    }

    private suspend fun prepopulateHistoryWithMissingDates(routineId: Long) {
        val lastDateInHistory = completionHistoryRepository.getLastHistoryEntryDate(routineId)
        val yesterday =
            Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(DatePeriod(days = 1))
        if (lastDateInHistory != null && lastDateInHistory < yesterday) {
            for (date in lastDateInHistory.plusDays(1)..yesterday) {
                insertRoutineStatusIntoHistory(
                    routineId = routineId,
                    currentDate = date,
                    completedOnCurrentDate = false,
                )
            }
        }
    }

    private suspend fun computeFutureDates(
        dateRange: LocalDateRange,
        routineId: Long
    ): List<StatusEntry?> {
        val statusList = mutableListOf<StatusEntry?>()
        for (date in dateRange) {
            val routine = routineRepository.getRoutineById(routineId)
            statusList.add(
                routine.computePlanningStatus(date)?.let {
                    StatusEntry(
                        date = date,
                        status = it,
                    )
                }
            )
        }
        return statusList
    }
}