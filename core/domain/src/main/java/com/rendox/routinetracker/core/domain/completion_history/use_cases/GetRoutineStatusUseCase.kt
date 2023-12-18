package com.rendox.routinetracker.core.domain.completion_history.use_cases

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine.RoutineRepository
import com.rendox.routinetracker.core.domain.completion_history.computePlanningStatus
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.model.onVacationHistoricalStatuses
import com.rendox.routinetracker.core.model.toStatusEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetRoutineStatusUseCase(
    private val routineRepository: RoutineRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val insertRoutineStatus: InsertRoutineStatusUseCase,
) {
    suspend operator fun invoke(
        routineId: Long, date: LocalDate, today: LocalDate
    ): RoutineStatus? = invoke(routineId, date..date, today).firstOrNull()?.status

    suspend operator fun invoke(
        routineId: Long,
        dates: LocalDateRange,
        today: LocalDate,
    ): List<StatusEntry> {
        println("get routine status")
        return withContext(Dispatchers.Default) {
            val habit: Habit = routineRepository.getRoutineById(routineId)
            prepopulateHistoryWithMissingDates(habit, today)

            val resultingStatusList = mutableListOf<StatusEntry>()

            val completionHistoryPart =
                completionHistoryRepository.getHistoryEntries(routineId, dates)
            resultingStatusList.addAll(completionHistoryPart.map { it.toStatusEntry() })

            if (completionHistoryPart.isEmpty()) {
                resultingStatusList.addAll(computeFutureStatuses(dates, habit))
            } else if (completionHistoryPart.last().date < dates.endInclusive) {
                val startDate = completionHistoryPart.last().date.plusDays(1)
                val endDate = dates.endInclusive
                resultingStatusList.addAll(computeFutureStatuses(startDate..endDate, habit))
            }
            resultingStatusList
        }
    }

    private suspend fun prepopulateHistoryWithMissingDates(habit: Habit, today: LocalDate) {
        val yesterday = today.minus(DatePeriod(days = 1))
        val lastDateInHistory = completionHistoryRepository.getLastHistoryEntry(habit.id!!)?.date

        val routineEndDate: LocalDate? = habit.schedule.endDate
        if (routineEndDate != null && yesterday > routineEndDate) return

        if (lastDateInHistory == null && habit.schedule.startDate <= yesterday) {
            for (date in habit.schedule.startDate..yesterday) {
                insertRoutineStatus(
                    routineId = habit.id!!,
                    currentDate = date,
                    completedOnCurrentDate = false,
                    today = today,
                )
            }
            return
        }

        if (lastDateInHistory != null && lastDateInHistory < yesterday) {
            for (date in lastDateInHistory.plusDays(1)..yesterday) {
                insertRoutineStatus(
                    routineId = habit.id!!,
                    currentDate = date,
                    completedOnCurrentDate = false,
                    today = today,
                )
            }
            return
        }
    }

    private suspend fun computeFutureStatuses(
        dateRange: LocalDateRange,
        habit: Habit,
    ): List<StatusEntry> {
        val lastVacationStatus = completionHistoryRepository.getLastHistoryEntryByStatus(
            routineId = habit.id!!,
            matchingStatuses = onVacationHistoricalStatuses,
        )

        val statusList = mutableListOf<StatusEntry>()
        val lastHistoryEntry = completionHistoryRepository.getLastHistoryEntry(habit.id!!)
        val schedule = habit.schedule
        val lastPeriod: LocalDateRange? = lastHistoryEntry?.let {
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(
                currentDate = it.date,
                lastVacationEndDate = lastVacationStatus?.date,
            )
            else null
        }
        val numOfTimesCompletedInLastPeriodAtTheMomentOfLastHistoryEntryDate =
            lastHistoryEntry?.let {
                completionHistoryRepository.getTotalTimesCompletedInPeriod(
                    routineId = habit.id!!,
                    startDate = lastPeriod?.start ?: habit.schedule.startDate,
                    endDate = it.date,
                )
            } ?: 0.0
        val periodSeparationEnabled =
            schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled
        val scheduleDeviationAtTheMomentOfLastHistoryEntryDate =
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = habit.id!!,
                startDate = if (periodSeparationEnabled && lastPeriod != null) {
                    lastPeriod.start
                } else {
                    schedule.startDate
                },
                endDate = dateRange.start,
            )
        val scheduleDeviationInCurrentPeriod = lastPeriod?.let {
            completionHistoryRepository.getScheduleDeviationInPeriod(
                routineId = habit.id!!,
                startDate = it.start,
                endDate = dateRange.start,
            )
        }

        for (date in dateRange) {
            habit.computePlanningStatus(
                validationDate = date,
                currentScheduleDeviation = scheduleDeviationAtTheMomentOfLastHistoryEntryDate,
                actualDate = lastHistoryEntry?.date,
                numOfTimesCompletedInCurrentPeriod = numOfTimesCompletedInLastPeriodAtTheMomentOfLastHistoryEntryDate,
                scheduleDeviationInCurrentPeriod = scheduleDeviationInCurrentPeriod,
                lastVacationEndDate = lastVacationStatus?.date,
            )?.let {
                statusList.add(StatusEntry(
                    date = date,
                    status = it,
                ))
            }
        }
        return statusList
    }
}