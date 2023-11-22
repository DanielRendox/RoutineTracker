package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.model.HistoricalStatus

suspend fun sortOutBacklog(routineId: Long, completionHistoryRepository: CompletionHistoryRepository) {
    val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
        routineId = routineId,
        matchingStatuses = listOf(HistoricalStatus.NotCompleted),
    )!!

    completionHistoryRepository.updateHistoryEntryByDate(
        routineId = routineId,
        date = lastNotCompleted.date,
        newStatus = HistoricalStatus.CompletedLater,
        newScheduleDeviation = lastNotCompleted.scheduleDeviation,
        newTimesCompleted = lastNotCompleted.timesCompleted,
    )
}

suspend fun undoSortingOutBacklog(
    routineId: Long,
    completionHistoryRepository: CompletionHistoryRepository,
) {
    val completedLaterEntry = completionHistoryRepository.getLastHistoryEntryByStatus(
        routineId = routineId,
        matchingStatuses = listOf(HistoricalStatus.CompletedLater),
    )!!

    completionHistoryRepository.updateHistoryEntryByDate(
        routineId = routineId,
        newStatus = HistoricalStatus.NotCompleted,
        date = completedLaterEntry.date,
        newScheduleDeviation = completedLaterEntry.scheduleDeviation,
        newTimesCompleted = completedLaterEntry.timesCompleted,
    )

    completionHistoryRepository.deleteCompletedLaterBackupEntry(routineId, completedLaterEntry.date)
}