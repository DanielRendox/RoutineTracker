package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.domain.streak.StartStreakOrJoinStreaksUseCase
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

suspend fun sortOutBacklog(
    habit: Habit,
    completionHistoryRepository: CompletionHistoryRepository,
    startStreakOrJoinStreaks: StartStreakOrJoinStreaksUseCase,
    currentDate: LocalDate
) {
    val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
        routineId = habit.id!!,
        matchingStatuses = listOf(HistoricalStatus.NotCompleted),
        maxDate = currentDate.minus(DatePeriod(days = 1)),
    )!!

    startStreakOrJoinStreaks(
        routine = habit,
        date = lastNotCompleted.date,
    )

    completionHistoryRepository.updateHistoryEntryByDate(
        routineId = habit.id!!,
        date = lastNotCompleted.date,
        newStatus = HistoricalStatus.CompletedLater,
        newScheduleDeviation = lastNotCompleted.scheduleDeviation,
        newTimesCompleted = lastNotCompleted.timesCompleted,
    )
}