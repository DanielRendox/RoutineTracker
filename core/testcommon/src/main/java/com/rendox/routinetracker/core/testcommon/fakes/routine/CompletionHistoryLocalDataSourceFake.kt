package com.rendox.routinetracker.core.testcommon.fakes.routine

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.datetime.LocalDate

class CompletionHistoryLocalDataSourceFake(
    private val routineData: RoutineData
) : CompletionHistoryLocalDataSource {

    override suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange
    ): List<CompletionHistoryEntry> =
        routineData.completionHistory
            .filter { it.first == routineId && it.second.date in dates }
            .map { it.second }

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
    ) {
        routineData.completionHistory =
            routineData.completionHistory.toMutableList().apply { add(Pair(routineId, entry)) }
        if (entry.status == HistoricalStatus.CompletedLater) {
            insertCompletedLaterDate(routineId, entry.date)
        }
    }

    override suspend fun deleteHistoryEntry(routineId: Long, date: LocalDate) {
        val elementToRemove = routineData.completionHistory.find {
            it.first == routineId && it.second.date == date
        }
        routineData.completionHistory =
            routineData.completionHistory.toMutableList().apply { remove(elementToRemove) }
    }

    override suspend fun updateHistoryEntryStatusByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus,
        newScheduleDeviation: Int,
    ) {
        val elementToUpdate = routineData.completionHistory.find {
            it.first == routineId && it.second.date == date
        }
        val elementToUpdateIndex = routineData.completionHistory.indexOf(elementToUpdate)
        val newValue = routineId to CompletionHistoryEntry(date, newStatus, newScheduleDeviation)
        elementToUpdate?.let {
            routineData.completionHistory =
                routineData.completionHistory.toMutableList().apply {
                    set(elementToUpdateIndex, newValue)
                }
            if (newStatus == HistoricalStatus.CompletedLater) {
                insertCompletedLaterDate(routineId, date)
            }
        }
    }

    override suspend fun getFirstHistoryEntry(routineId: Long): CompletionHistoryEntry? =
        routineData.completionHistory.firstOrNull()?.second

    override suspend fun getLastHistoryEntry(routineId: Long): CompletionHistoryEntry? =
        routineData.completionHistory.lastOrNull()?.second

    override suspend fun getFirstHistoryEntryByStatus(
        routineId: Long, matchingStatuses: List<HistoricalStatus>
    ): CompletionHistoryEntry? {
        return routineData.completionHistory.firstOrNull {
            it.first == routineId && it.second.status in matchingStatuses
        }?.second
    }

    override suspend fun getLastHistoryEntryByStatus(
        routineId: Long, matchingStatuses: List<HistoricalStatus>
    ): CompletionHistoryEntry? {
        return routineData.completionHistory.lastOrNull {
            it.first == routineId && it.second.status in matchingStatuses
        }?.second
    }

    override suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean =
        routineData.completedLaterHistory.contains(Pair(routineId, date))

    override suspend fun deleteCompletedLaterBackupEntry(routineId: Long, date: LocalDate) {
        routineData.completedLaterHistory =
            routineData.completedLaterHistory.toMutableList()
                .apply { remove(Pair(routineId, date)) }
    }

    private fun checkCompletedLater(routineId: Long, date: LocalDate): Boolean =
        routineData.completedLaterHistory.contains(Pair(routineId, date))

    private fun insertCompletedLaterDate(routineId: Long, date: LocalDate) {
        val alreadyInserted = checkCompletedLater(routineId, date)
        if (!alreadyInserted) {
            routineData.completedLaterHistory =
                routineData.completedLaterHistory.toMutableList().apply {
                    add(Pair(routineId, date))
                }
        }
    }
}