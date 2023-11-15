package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.routine.schedule.getPeriodRange
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetListOfStreaksUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val routineRepository: RoutineRepository,
) {

    private val positiveHistoricalStatuses = listOf(
        HistoricalStatus.Completed,
        HistoricalStatus.OverCompleted,
        HistoricalStatus.OverCompletedOnVacation,
        HistoricalStatus.SortedOutBacklog,
        HistoricalStatus.SortedOutBacklogOnVacation,
    )

    private val negativeHistoricalStatuses = listOf(HistoricalStatus.NotCompleted)

    suspend operator fun invoke(routineId: Long): List<Streak> {
        val routineStartDate =
            completionHistoryRepository.getFirstHistoryEntry(routineId)?.date ?: return emptyList()

        val schedule = routineRepository.getRoutineById(routineId).schedule
        if (schedule is Schedule.CustomDateSchedule) return emptyList()

        val streaks = mutableListOf<Streak>()
        var streakStart: LocalDate
        var dateStartLookingFrom: LocalDate = routineStartDate

        while (true) {
            println("GetListOfStreaksUseCase infinite while loop")
            streakStart = findNextPositiveStatus(routineId, dateStartLookingFrom) ?: return streaks

            var nextNegativeStatusDate: LocalDate?
            if (schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled) {
                val periodStart = schedule.getPeriodRange(streakStart).start
                nextNegativeStatusDate = findNextNegativeStatus(routineId, periodStart)
                println("period range = ${schedule.getPeriodRange(streakStart)}")
                println("streakStart = $streakStart")
                println("nextNegativeStatusDate = $nextNegativeStatusDate")
                val startStreakFromPeriodStart =
                    nextNegativeStatusDate == null || streakStart < nextNegativeStatusDate
                if (startStreakFromPeriodStart) {
                    streakStart = periodStart
                } else {
                    nextNegativeStatusDate = findNextNegativeStatus(routineId, streakStart)
                }
            } else if(dateStartLookingFrom == routineStartDate) {
                nextNegativeStatusDate = findNextNegativeStatus(routineId, routineStartDate)
                val startStreakFromPeriodStart =
                    nextNegativeStatusDate == null || streakStart < nextNegativeStatusDate
                if (startStreakFromPeriodStart) streakStart = routineStartDate
            } else {
                nextNegativeStatusDate = findNextNegativeStatus(routineId, streakStart)
            }

            streaks.add(Streak(streakStart, nextNegativeStatusDate?.minus(DatePeriod(days = 1))))
            dateStartLookingFrom = nextNegativeStatusDate ?: return streaks
        }
    }

    private suspend fun findNextNegativeStatus(
        routineId: Long, startingFromDate: LocalDate
    ): LocalDate? {
        return completionHistoryRepository.getFirstHistoryEntryDateByStatus(
            routineId = routineId,
            startingFromDate = startingFromDate,
            matchingStatuses = negativeHistoricalStatuses,
        )
    }

    private suspend fun findNextPositiveStatus(
        routineId: Long, startingFromDate: LocalDate
    ): LocalDate? {
        return completionHistoryRepository.getFirstHistoryEntryDateByStatus(
            routineId = routineId,
            startingFromDate = startingFromDate,
            matchingStatuses = positiveHistoricalStatuses,
        )
    }
}