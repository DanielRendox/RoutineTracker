package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.datetime.LocalDate

class CompletionHistoryLocalDataSourceFake : CompletionHistoryLocalDataSource {

    private val lock = Any()

    private var completionHistory = emptyList<Pair<Long, CompletionHistoryEntry>>()
        get() {
            synchronized(lock) {
                return field
            }
        }
        set(value) {
            synchronized(lock) {
                field = value
            }
        }

    private var scheduleDeviation = 0
        get() {
            synchronized(lock) {
                return field
            }
        }
        set(value) {
            synchronized(lock) {
                field = value
            }
        }

    private fun addToCompletionHistory(routineId: Long, entry: CompletionHistoryEntry) {
        completionHistory = completionHistory.toMutableList().apply { add(Pair(routineId, entry)) }
    }

    override suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange
    ): List<CompletionHistoryEntry> =
        completionHistory
            .filter { it.first == routineId }
            .map { it.second }
            .filter { it.date in dates }

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
        tasksCompletedCounterIncrementAmount: Int?
    ) {
        addToCompletionHistory(routineId, entry)
        tasksCompletedCounterIncrementAmount?.let { incrementAmount ->
            scheduleDeviation += incrementAmount
        }
    }

    override suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        status: HistoricalStatus,
        tasksCompletedCounterIncrementAmount: Int?
    ) {
        val entry =
            completionHistory.find { it.first == routineId && it.second.date == date }
        val entryId = completionHistory.indexOf(entry)
        entry?.let {
            completionHistory = completionHistory.toMutableList().apply {
                set(entryId, it.copy(second = CompletionHistoryEntry(date, status)))
            }
        }
        tasksCompletedCounterIncrementAmount?.let { incrementAmount ->
            scheduleDeviation += incrementAmount
        }
    }

    override suspend fun updateHistoryEntryStatusByStatus(
        routineId: Long,
        newStatus: HistoricalStatus,
        tasksCompletedCounterIncrementAmount: Int?,
        matchingStatuses: List<HistoricalStatus>
    ) {
        val entry = completionHistory.findLast {
            it.first == routineId && matchingStatuses.contains(it.second.status)
        }
        val entryId = completionHistory.indexOf(entry)
        entry?.let {
            completionHistory = completionHistory.toMutableList().apply {
                set(entryId, it.copy(second = CompletionHistoryEntry(it.second.date, newStatus)))
            }
        }
        tasksCompletedCounterIncrementAmount?.let { incrementAmount ->
            scheduleDeviation += incrementAmount
        }
    }

    override suspend fun getLastHistoryEntryDate(routineId: Long): LocalDate? {
        if (completionHistory.isEmpty()) return null
        return completionHistory.last().second.date
    }
}