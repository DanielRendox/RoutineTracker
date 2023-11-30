package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.domain.streak.StartStreakOrJoinStreaksUseCase
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Routine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

suspend fun sortOutBacklog(
    routine: Routine,
    completionHistoryRepository: CompletionHistoryRepository,
    startStreakOrJoinStreaks: StartStreakOrJoinStreaksUseCase,
    currentDate: LocalDate
) {
    val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
        routineId = routine.id!!,
        matchingStatuses = listOf(HistoricalStatus.NotCompleted),
        maxDate = currentDate.minus(DatePeriod(days = 1)),
    )!!

    startStreakOrJoinStreaks(
        routine = routine,
        date = lastNotCompleted.date,
    )

    completionHistoryRepository.updateHistoryEntryByDate(
        routineId = routine.id!!,
        date = lastNotCompleted.date,
        newStatus = HistoricalStatus.CompletedLater,
        newScheduleDeviation = lastNotCompleted.scheduleDeviation,
        newTimesCompleted = lastNotCompleted.timesCompleted,
    )
}