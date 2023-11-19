package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun Streak.contains(date: LocalDate): Boolean {
    val streakEnd = end
    return date >= start && (streakEnd == null || streakEnd <= date)
}

fun Streak.getDurationInDays(today: LocalDate): Int {
    val endDate = this.end ?: today
    return this.start.daysUntil(endDate) + 1
}

fun getCurrentStreakDurationInDays(
    lastStreak: Streak?, today: LocalDate, schedule: Schedule
): Int? {
    if (schedule is Schedule.CustomDateSchedule) return null
    if (lastStreak == null) return 0
    val lastStreakEnd = lastStreak.end
    return if (lastStreakEnd == null || lastStreakEnd >= today) {
        lastStreak.copy(end = today).getDurationInDays(today)
    } else 0
}

fun getLongestStreakDurationInDays(
    allStreaks: List<Streak>, today: LocalDate, schedule: Schedule
): Int? {
    if (schedule is Schedule.CustomDateSchedule) return null
    return allStreaks.map { it.getDurationInDays(today) }.maxByOrNull { it } ?: 0
}

fun deriveDatesIncludedInStreak(
    streaks: List<Streak>, dateRange: LocalDateRange
): List<LocalDate> {
    val streakDates = mutableListOf<LocalDate>()
    for (streak in streaks) {
        val streakEnd = streak.end
        val streakStartNotGreaterThanLastDate = streak.start <= dateRange.endInclusive
        val streakEndNotLessThanFirstDate = (streakEnd == null) || streakEnd >= dateRange.start
        val streakIncludesPartOfGivenRange =
            streakStartNotGreaterThanLastDate && streakEndNotLessThanFirstDate
        if (streakIncludesPartOfGivenRange) {
            val startDateWithinRange: LocalDate =
                if (streak.start <= dateRange.start) dateRange.start else streak.start
            val endDateWithinRange: LocalDate =
                if (streakEnd == null || streakEnd >= dateRange.endInclusive) dateRange.endInclusive
                else streakEnd
            for (date in startDateWithinRange..endDateWithinRange) streakDates.add(date)
        }
    }
    return streakDates
}

suspend fun sortOutBacklog(routineId: Long, completionHistoryRepository: CompletionHistoryRepository) {
    val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryDateByStatus(
        routineId = routineId,
        matchingStatuses = listOf(HistoricalStatus.NotCompleted),
    )!!

    completionHistoryRepository.updateHistoryEntryStatusByDate(
        routineId = routineId,
        date = lastNotCompleted.date,
        newStatus = HistoricalStatus.CompletedLater,
        newScheduleDeviation = lastNotCompleted.currentScheduleDeviation,
    )
}

suspend fun undoSortingOutBacklog(
    routineId: Long,
    completionHistoryRepository: CompletionHistoryRepository,
) {
    val completedLaterEntry = completionHistoryRepository.getLastHistoryEntryDateByStatus(
        routineId = routineId,
        matchingStatuses = listOf(HistoricalStatus.CompletedLater),
    )!!

    completionHistoryRepository.updateHistoryEntryStatusByDate(
        routineId = routineId,
        newStatus = HistoricalStatus.NotCompleted,
        date = completedLaterEntry.date,
        newScheduleDeviation = completedLaterEntry.currentScheduleDeviation,
    )
}