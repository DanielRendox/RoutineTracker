package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.domain.completion_history.HabitStatusComputer
import com.rendox.routinetracker.core.domain.completion_history.getPeriodRange
import com.rendox.routinetracker.core.domain.completion_history.isDue
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
        var failedDate: LocalDate?

        for (completedDate in completionHistory.map { it.date }) {
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

            failedDate = findNextFailedDate(
                habit = habit,
                currentDate = completedDate,
                today = today,
                maxDate = lastPossibleStreakDate,
                completionHistory = completionHistory,
                vacationHistory = vacationHistory,
            )

            val streakShouldEndAtPeriodEnd =
                completedDatePeriod != null && !completedDatePeriod.contains(failedDate)
            val periodEnd = completedDatePeriod?.endInclusive
            val streakEndDate = when {
                streakShouldEndAtPeriodEnd && periodEnd!! < lastPossibleStreakDate -> periodEnd
                failedDate != null -> failedDate.minus(DatePeriod(days = 1))
                lastPossibleStreakDate == today -> {
                    val todayStatus = habitStatusComputer.computeStatus(
                        validationDate = today,
                        today = today,
                        habit = habit,
                        completionHistory = completionHistory,
                        vacationHistory = vacationHistory,
                    )
                    if (todayStatus == HabitStatus.Planned) {
                        today.minus(DatePeriod(days = 1))
                    } else {
                        today
                    }
                }

                else -> lastPossibleStreakDate
            }

            val currentStreak = Streak(startDate = streakStartDate, endDate = streakEndDate)
            val someStreakAlreadyIncludesCurrentStreak = streaks.any { saveStreak ->
                saveStreak.startDate <= currentStreak.startDate &&
                        saveStreak.endDate >= currentStreak.endDate
            }
            if (someStreakAlreadyIncludesCurrentStreak) continue

            val streakToMerge =
                streaks.find { it.endDate.plusDays(1) == currentStreak.startDate }
            if (streakToMerge != null) {
                streaks.remove(streakToMerge)
                streaks.add(currentStreak.copy(startDate = streakToMerge.startDate))
            } else {
                streaks.add(currentStreak)
            }
        }

        return streaks
    }

    override fun computeStreaksInPeriod(
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
        period: LocalDateRange
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
        if (!habit.schedule.backlogEnabled) return completedDate
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