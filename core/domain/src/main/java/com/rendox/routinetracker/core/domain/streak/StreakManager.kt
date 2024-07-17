package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.schedule.expandPeriodToScheduleBounds
import com.rendox.routinetracker.core.domain.schedule.getPeriodRange
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.logic.time.withDayOfMonth
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import kotlinx.datetime.LocalDate

class StreakManager(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val vacationHistoryRepository: VacationRepository,
    private val streakComputer: StreakComputer,
) {

    suspend fun formStreaksAfterCompletion(
        habit: Habit,
        completion: Habit.CompletionRecord,
        today: LocalDate,
    ): Pair<LocalDateRange, List<Streak>>? = formStreaks(habit, today, completion)

    suspend fun formLastNotSavedStreaks(
        habit: Habit,
        today: LocalDate,
    ): Pair<LocalDateRange, List<Streak>> = formStreaks(habit, today, null)!!

    private suspend fun formStreaks(
        habit: Habit,
        today: LocalDate,
        completion: Habit.CompletionRecord?,
    ): Pair<LocalDateRange, List<Streak>>? {
        val currentPeriod = getPeriodRange(habit.schedule, today)
        val completionsWithoutStreaks = completionHistoryRepository
            .getRecordsWithoutStreaks(habit)
            .map { it.date }
            .toMutableList()
            .apply { if (completion != null) add(completion.date) }
            .filter { date -> date !in currentPeriod }
        if (completionsWithoutStreaks.isEmpty()) return null

        val queryPeriod = completionsWithoutStreaks.min()..completionsWithoutStreaks.max()
        val expandedPeriod = queryPeriod.expandPeriodToScheduleBounds(
            schedule = habit.schedule,
            getPeriodRange = { date -> getPeriodRange(habit.schedule, date) },
        )
        return expandedPeriod to computeStreaks(habit, expandedPeriod, today, completion)
    }

    private suspend fun computeStreaks(
        habit: Habit,
        period: LocalDateRange,
        today: LocalDate,
        completion: Habit.CompletionRecord?,
    ): List<Streak> {
        val completionHistory = completionHistoryRepository.getRecordsInPeriod(habit, period)
            .toMutableList()
            .apply {
                if (completion?.date in period) {
                    val index = indexOfFirst { it.date == completion?.date }
                    if (index != -1) removeAt(index)
                    if (completion != null) add(completion)
                }
            }
        val vacationHistory = vacationHistoryRepository.getVacationsInPeriod(habit.id!!, period)
        return streakComputer.computeStreaks(
            today = today,
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )
    }

    companion object {
        private fun getPeriodRange(
            schedule: Schedule,
            currentDate: LocalDate,
        ): LocalDateRange = when (schedule) {
            is Schedule.PeriodicSchedule -> schedule.getPeriodRange(currentDate)
            is Schedule.EveryDaySchedule -> currentDate.withDayOfMonth(1)..currentDate.atEndOfMonth
            is Schedule.CustomDateSchedule -> throw IllegalArgumentException()
        }
    }
}