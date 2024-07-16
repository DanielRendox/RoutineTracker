package com.rendox.routinetracker.core.domain.streak.computer

import com.rendox.routinetracker.core.domain.habitstatus.HabitStatusComputer
import com.rendox.routinetracker.core.domain.schedule.getPeriodRange
import com.rendox.routinetracker.core.domain.schedule.isDue
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation
import com.rendox.routinetracker.core.model.streakCreatorStatuses
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

internal class StreakComputerImpl(
    private val habitStatusComputer: HabitStatusComputer,
) : StreakComputer {
    override fun computeStreaks(
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): List<Streak> {
        if (completionHistory.isEmpty()) return emptyList()
        val streaks = mutableListOf<Streak>()
        val sortedCompletionHistory = completionHistory.sortedBy { it.date }

        val lastPossibleStreakDate = deriveLastPossibleStreakDate(
            habit = habit,
            today = today,
        )

        var completedDate = sortedCompletionHistory.first().date
        while (completedDate <= sortedCompletionHistory.last().date) {
            val streak = computeStreak(
                today = today,
                habit = habit,
                completedDate = completedDate,
                completionHistory = sortedCompletionHistory,
                vacationHistory = vacationHistory,
                lastPossibleStreakDate = lastPossibleStreakDate,
            )
            if (streak == null) {
                completedDate =
                    sortedCompletionHistory.firstOrNull { it.date > completedDate }?.date ?: break
                continue
            }

            streaks.add(streak)
            completedDate =
                sortedCompletionHistory.firstOrNull { it.date > streak.endDate }?.date ?: break
        }
        return streaks
    }

    private fun computeStreak(
        today: LocalDate,
        habit: Habit,
        completedDate: LocalDate,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
        lastPossibleStreakDate: LocalDate,
    ): Streak? {
        val habitStatus = habitStatusComputer.computeStatus(
            validationDate = completedDate,
            today = today,
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )
        if (habitStatus !in streakCreatorStatuses) return null

        val schedule = habit.schedule
        val completedDatePeriod: LocalDateRange? = if (schedule is Schedule.PeriodicSchedule) {
            schedule.getPeriodRange(currentDate = completedDate)
        } else {
            null
        }

        val streakStartDate = findStreakStartDate(
            habit = habit,
            completedDate = completedDate,
            completedDatePeriod = completedDatePeriod,
            today = today,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )

        val failedDate = findNextFailedDate(
            habit = habit,
            currentDate = completedDate,
            today = today,
            maxDate = lastPossibleStreakDate,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )

        val streakEndDate = findStreakEndDate(
            habit = habit,
            failedDate = failedDate,
            maxDate = lastPossibleStreakDate,
            today = today,
            completedDatePeriod = completedDatePeriod,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )

        return Streak(startDate = streakStartDate, endDate = streakEndDate)
    }

    private fun deriveLastPossibleStreakDate(
        habit: Habit,
        today: LocalDate,
    ): LocalDate {
        val habitEndDate = habit.schedule.endDate
        return when {
            habitEndDate != null && habitEndDate < today -> habitEndDate
            else -> today
        }
    }

    private fun findStreakStartDate(
        habit: Habit,
        completedDate: LocalDate,
        completedDatePeriod: LocalDateRange?,
        today: LocalDate,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): LocalDate {
        val previousFailedDate = findPreviousFailedDate(
            habit = habit,
            currentDate = completedDate,
            minDate = completedDatePeriod?.start ?: habit.schedule.startDate,
            today = today,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )
        return when (previousFailedDate) {
            null -> completedDatePeriod?.start ?: habit.schedule.startDate
            else -> previousFailedDate.plusDays(1)
        }
    }

    private fun findStreakEndDate(
        habit: Habit,
        failedDate: LocalDate?,
        maxDate: LocalDate,
        today: LocalDate,
        completedDatePeriod: LocalDateRange?,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): LocalDate {
        val streakShouldEndAtPeriodEnd =
            completedDatePeriod != null && !completedDatePeriod.contains(failedDate)
        val periodEnd = completedDatePeriod?.endInclusive
        return when {
            streakShouldEndAtPeriodEnd && periodEnd!! < maxDate -> periodEnd
            failedDate != null -> failedDate.minus(DatePeriod(days = 1))
            maxDate == today -> {
                val todayStatus = habitStatusComputer.computeStatus(
                    validationDate = today,
                    today = today,
                    habit = habit,
                    completionHistory = completionHistory,
                    vacationHistory = vacationHistory,
                )
                when (todayStatus) {
                    HabitStatus.Planned, HabitStatus.Backlog -> today.minus(DatePeriod(days = 1))
                    else -> today
                }
            }

            else -> maxDate
        }
    }

    private fun findNextFailedDate(
        habit: Habit,
        currentDate: LocalDate,
        today: LocalDate,
        maxDate: LocalDate,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): LocalDate? {
        for (date in currentDate..maxDate) {
            if (habit.schedule.isDue(validationDate = date)) {
                val habitStatus = habitStatusComputer.computeStatus(
                    validationDate = date,
                    today = today,
                    habit = habit,
                    completionHistory = completionHistory,
                    vacationHistory = vacationHistory,
                )
                if (habitStatus == HabitStatus.Failed) return date
            }
        }
        return null
    }

    private fun findPreviousFailedDate(
        habit: Habit,
        currentDate: LocalDate,
        minDate: LocalDate,
        today: LocalDate,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): LocalDate? {
        var date = currentDate
        while (date >= minDate) {
            if (habit.schedule.isDue(validationDate = date)) {
                val habitStatus = habitStatusComputer.computeStatus(
                    validationDate = date,
                    today = today,
                    habit = habit,
                    completionHistory = completionHistory,
                    vacationHistory = vacationHistory,
                )
                if (habitStatus == HabitStatus.Failed) return date
            }
            date = date.minus(DatePeriod(days = 1))
        }
        return null
    }
}