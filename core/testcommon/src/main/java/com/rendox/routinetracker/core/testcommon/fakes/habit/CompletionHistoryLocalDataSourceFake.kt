package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.CompletionHistoryEntry
import com.rendox.routinetracker.core.model.HistoricalStatus
import kotlinx.datetime.LocalDate

class CompletionHistoryLocalDataSourceFake(
    private val habitData: HabitData
) : CompletionHistoryLocalDataSource {

    override suspend fun getHistoryEntries(
        routineId: Long,
        dates: LocalDateRange
    ): List<CompletionHistoryEntry> =
        habitData.routineCompletionHistory
            .filter { it.first == routineId && it.second.date in dates }
            .map { it.second }
            .sortedBy { it.date }

    override suspend fun getHistoryEntryByDate(
        routineId: Long,
        date: LocalDate
    ): CompletionHistoryEntry? {
        return habitData.routineCompletionHistory.firstOrNull {
            it.first == routineId && it.second.date == date
        }?.second
    }

    override suspend fun insertHistoryEntry(
        id: Long?,
        routineId: Long,
        entry: CompletionHistoryEntry,
    ) {
        habitData.routineCompletionHistory =
            habitData.routineCompletionHistory.toMutableList().apply { add(Pair(routineId, entry)) }
        if (entry.status == HistoricalStatus.CompletedLater) {
            insertCompletedLaterDate(routineId, entry.date)
        }
    }

    override suspend fun deleteHistoryEntry(routineId: Long, date: LocalDate) {
        val elementToRemove = habitData.routineCompletionHistory.find {
            it.first == routineId && it.second.date == date
        }
        habitData.routineCompletionHistory =
            habitData.routineCompletionHistory.toMutableList().apply { remove(elementToRemove) }
    }

    override suspend fun updateHistoryEntryByDate(
        routineId: Long,
        date: LocalDate,
        newStatus: HistoricalStatus?,
        newScheduleDeviation: Float?,
        newTimesCompleted: Float?,
    ) {
        val elementToUpdate = habitData.routineCompletionHistory.find {
            it.first == routineId && it.second.date == date
        }
        elementToUpdate?.let {
            val elementToUpdateIndex = habitData.routineCompletionHistory.indexOf(it)
            val newValue = routineId to CompletionHistoryEntry(
                date = date,
                status = newStatus ?: it.second.status,
                scheduleDeviation = newScheduleDeviation ?: it.second.scheduleDeviation,
                timesCompleted = newTimesCompleted ?: it.second.timesCompleted,
            )
            habitData.routineCompletionHistory =
                habitData.routineCompletionHistory.toMutableList().apply {
                    set(elementToUpdateIndex, newValue)
                }
            if (newStatus == HistoricalStatus.CompletedLater) {
                insertCompletedLaterDate(routineId, date)
            }
        }
    }

    override suspend fun getFirstHistoryEntry(routineId: Long): CompletionHistoryEntry? =
        habitData.routineCompletionHistory.firstOrNull()?.second

    override suspend fun getLastHistoryEntry(routineId: Long): CompletionHistoryEntry? =
        habitData.routineCompletionHistory.lastOrNull()?.second

    override suspend fun getFirstHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): CompletionHistoryEntry? {
        return habitData.routineCompletionHistory.firstOrNull {
            it.first == routineId
                    && it.second.status in matchingStatuses
                    && (minDate == null || minDate <= it.second.date)
                    && (maxDate == null || it.second.date <= maxDate)
        }?.second
    }

    override suspend fun getLastHistoryEntryByStatus(
        routineId: Long,
        matchingStatuses: List<HistoricalStatus>,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): CompletionHistoryEntry? {
        return habitData.routineCompletionHistory.lastOrNull {
            it.first == routineId
                    && it.second.status in matchingStatuses
                    && (minDate == null || minDate <= it.second.date)
                    && (maxDate == null || it.second.date <= maxDate)
        }?.second
    }

    override suspend fun checkIfStatusWasCompletedLater(routineId: Long, date: LocalDate): Boolean =
        habitData.completedLaterHistory.contains(Pair(routineId, date))

    override suspend fun deleteCompletedLaterBackupEntry(routineId: Long, date: LocalDate) {
        habitData.completedLaterHistory =
            habitData.completedLaterHistory.toMutableList()
                .apply { remove(Pair(routineId, date)) }
    }

    private fun checkCompletedLater(routineId: Long, date: LocalDate): Boolean =
        habitData.completedLaterHistory.contains(Pair(routineId, date))

    private fun insertCompletedLaterDate(routineId: Long, date: LocalDate) {
        val alreadyInserted = checkCompletedLater(routineId, date)
        if (!alreadyInserted) {
            habitData.completedLaterHistory =
                habitData.completedLaterHistory.toMutableList().apply {
                    add(Pair(routineId, date))
                }
        }
    }

    override suspend fun getTotalTimesCompletedInPeriod(
        routineId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double = habitData.routineCompletionHistory
        .filter { it.second.date in startDate..endDate }
        .map { it.second.timesCompleted }
        .sum().toDouble()

    override suspend fun getScheduleDeviationInPeriod(
        routineId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        return habitData.routineCompletionHistory
            .filter { it.first == routineId && it.second.date in startDate..endDate }
            .map { it.second.scheduleDeviation }
            .sum().toDouble()
    }
}