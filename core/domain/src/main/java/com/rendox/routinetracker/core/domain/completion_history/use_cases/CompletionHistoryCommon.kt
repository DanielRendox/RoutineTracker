package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.streak.StreakRepository
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

val completedStatuses = listOf(
    HistoricalStatus.Completed,
    HistoricalStatus.OverCompleted,
    HistoricalStatus.OverCompletedOnVacation,
    HistoricalStatus.SortedOutBacklog,
    HistoricalStatus.SortedOutBacklogOnVacation,
)

val onVacationHistoricalStatuses = listOf(
    HistoricalStatus.NotCompletedOnVacation,
    HistoricalStatus.OverCompletedOnVacation,
    HistoricalStatus.SortedOutBacklogOnVacation,
)

val sortedOutBacklogStatuses = listOf(
    HistoricalStatus.SortedOutBacklog,
    HistoricalStatus.SortedOutBacklogOnVacation,
)

val overCompletedStatuses = listOf(
    HistoricalStatus.OverCompleted,
    HistoricalStatus.OverCompletedOnVacation,
)

suspend fun sortOutBacklog(
    routine: Routine,
    completionHistoryRepository: CompletionHistoryRepository,
    streakRepository: StreakRepository,
    currentDate: LocalDate
) {
    val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
        routineId = routine.id!!,
        matchingStatuses = listOf(HistoricalStatus.NotCompleted),
        maxDate = currentDate.minus(DatePeriod(days = 1)),
    )!!

    startStreakOrJoinStreaks(
        routine = routine,
        streakRepository = streakRepository,
        completionHistoryRepository = completionHistoryRepository,
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

suspend fun startStreakOrJoinStreaks(
    routine: Routine,
    streakRepository: StreakRepository,
    completionHistoryRepository: CompletionHistoryRepository,
    date: LocalDate,
) {
    println("startStreakOrJoinStreaks")
    val previousStreak = streakRepository.getStreakByDate(
        routineId = routine.id!!,
        dateWithinStreak = date.minus(DatePeriod(days = 1)),
    )
    val nextCompletedEntry = completionHistoryRepository.getFirstHistoryEntryByStatus(
        routineId = routine.id!!,
        matchingStatuses = completedStatuses,
        minDate = date.plusDays(1),
    )

    val nextStreak: Streak? = nextCompletedEntry?.let {
        streakRepository.getStreakByDate(
            routineId = routine.id!!,
            dateWithinStreak = it.date,
        )
    }

    println("previousStreak = $previousStreak")
    println("nextStreak = $nextStreak")

    if (previousStreak != null) {
        if (nextStreak != null) {
            streakRepository.updateStreakById(
                id = previousStreak.id!!,
                start = previousStreak.startDate,
                end = nextStreak.endDate,
            )
            if (previousStreak.id != nextStreak.id) {
                streakRepository.deleteStreakById(id = nextStreak.id!!)
            }
        } else {
            streakRepository.updateStreakById(
                id = previousStreak.id!!,
                start = previousStreak.startDate,
                end = null,
            )
        }
    } else {
        val lastNotCompleted = completionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = routine.id!!,
            matchingStatuses = listOf(HistoricalStatus.NotCompleted),
        )
        val streakStart =
            if (lastNotCompleted == null) routine.schedule.routineStartDate else date

        if (nextStreak != null) {
            streakRepository.updateStreakById(
                id = nextStreak.id!!,
                start = streakStart,
                end = nextStreak.endDate,
            )
        } else {
            streakRepository.insertStreak(
                routineId = routine.id!!,
                streak = Streak(
                    startDate = streakStart,
                    endDate = null
                ),
            )
        }
    }
}

suspend fun breakStreak(
    routineId: Long,
    date: LocalDate,
    completionHistoryRepository: CompletionHistoryRepository,
    streakRepository: StreakRepository,
) {
    val oldStreak = streakRepository.getStreakByDate(
        routineId = routineId,
        dateWithinStreak = date,
    )

    if (oldStreak != null) {
        streakRepository.deleteStreakById(id = oldStreak.id!!)

        if (oldStreak.startDate != date) {
            streakRepository.insertStreak(
                routineId = routineId,
                streak = Streak(
                    startDate = oldStreak.startDate,
                    endDate = date.minus(DatePeriod(days = 1)),
                ),
            )
        }

        val completedEntry = completionHistoryRepository.getFirstHistoryEntryByStatus(
            routineId = routineId,
            matchingStatuses = completedStatuses,
            minDate = date.plusDays(1),
        )

        if (completedEntry != null) {
            streakRepository.insertStreak(
                routineId = routineId,
                streak = Streak(
                    startDate = completedEntry.date,
                    endDate = oldStreak.endDate,
                )
            )
        }
    }
}