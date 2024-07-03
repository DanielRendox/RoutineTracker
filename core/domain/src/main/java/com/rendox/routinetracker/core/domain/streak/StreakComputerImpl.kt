package com.rendox.routinetracker.core.domain.streak

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
    override fun computeAllStreaks(
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): List<Streak> {
        if (completionHistory.isEmpty()) return emptyList()

        val habitEndDate = habit.schedule.endDate
        val lastPossibleStreakDate = when {
            habitEndDate != null && habitEndDate < today -> habitEndDate
            else -> today
        }

        val streaks = mutableListOf<Streak>()

        var completedDate = completionHistory.first().date
        while (completedDate <= completionHistory.last().date) {
            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = completedDate,
                today = today,
                habit = habit,
                completionHistory = completionHistory,
                vacationHistory = vacationHistory,
            )
            if (habitStatus !in streakCreatorStatuses) continue

            val schedule = habit.schedule
            val completedDatePeriod: LocalDateRange? =
                if (schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled) {
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
                completionHistory = completionHistory,
                vacationHistory = vacationHistory,
            )

            val currentStreak = Streak(startDate = streakStartDate, endDate = streakEndDate)
            streaks.add(currentStreak)
            completedDate = completionHistory.firstOrNull { it.date > streakEndDate }?.date ?: break
        }

        return streaks
    }

    override fun computeStreaksInPeriod(
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
        period: LocalDateRange,
    ): List<Streak> = computeAllStreaks(
        today = today,
        habit = habit,
        completionHistory = completionHistory.filter { it.date in period },
        vacationHistory = vacationHistory,
    )

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
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): LocalDate = when {
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