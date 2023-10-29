package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.LocalDate

class CompletionHistoryLocalDataSourceFake(
    private val routineData: RoutineData
) : CompletionHistoryLocalDataSource {

    private fun addToCompletionHistory(routineId: Long, entry: CompletionHistoryEntry) {
        routineData.completionHistory =
            routineData.completionHistory.toMutableList().apply { add(Pair(routineId, entry)) }
    }

    override suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange
    ): List<CompletionHistoryEntry> =
        routineData.completionHistory
            .filter { it.first == routineId }
            .map { it.second }
            .filter { it.date in dates }

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
        scheduleDeviationIncrementAmount: Int
    ) {
        addToCompletionHistory(routineId, entry)
        incrementScheduleDeviation(
            incrementAmount = scheduleDeviationIncrementAmount,
            routineId = routineId,
        )
    }

    override suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        status: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int
    ) {
        val entry =
            routineData.completionHistory.find { it.first == routineId && it.second.date == date }
        val entryId = routineData.completionHistory.indexOf(entry)
        entry?.let {
            routineData.completionHistory = routineData.completionHistory.toMutableList().apply {
                set(entryId, it.copy(second = CompletionHistoryEntry(date, status)))
            }
        }
        incrementScheduleDeviation(
            incrementAmount = scheduleDeviationIncrementAmount,
            routineId = routineId,
        )
    }

    override suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        scheduleDeviationIncrementAmount: Int,
        matchingStatuses: List<HistoricalStatus>
    ) {
        val entry = routineData.completionHistory.findLast {
            it.first == routineId && matchingStatuses.contains(it.second.status)
        }
        val entryId = routineData.completionHistory.indexOf(entry)
        entry?.let {
            routineData.completionHistory = routineData.completionHistory.toMutableList().apply {
                set(entryId, it.copy(second = CompletionHistoryEntry(it.second.date, newStatus)))
            }
        }
        incrementScheduleDeviation(
            incrementAmount = scheduleDeviationIncrementAmount,
            routineId = routineId,
        )
    }

    override suspend fun getLastHistoryEntryDate(routineId: Long): LocalDate? {
        if (routineData.completionHistory.isEmpty()) return null
        return routineData.completionHistory.last().second.date
    }

    private fun incrementScheduleDeviation(incrementAmount: Int, routineId: Long) {
        val newRoutine =
            when (val oldRoutine = routineData.listOfRoutines[(routineId - 1).toInt()]) {
                is Routine.YesNoRoutine -> oldRoutine.copy(
                    scheduleDeviation = oldRoutine.scheduleDeviation + incrementAmount
                )
            }
        routineData.listOfRoutines = routineData.listOfRoutines.toMutableList().apply {
            set((routineId - 1).toInt(), newRoutine)
        }
    }
}