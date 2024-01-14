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
import com.rendox.routinetracker.core.model.streakCreatorStatuses
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

internal class StreakComputerImpl(
    private val habit: Habit,
    private val completionHistory: List<Habit.CompletionRecord>,
    private val habitStatusComputer: HabitStatusComputer,
) : StreakComputer {
    override fun computeAllStreaks(today: LocalDate): List<Streak> {
        if (completionHistory.isEmpty()) return emptyList()

        val habitEndDate = habit.schedule.endDate
        val lastPossibleStreakDate = when {
            habitEndDate != null && habitEndDate < today -> habitEndDate
            else -> today
        }

        val streaks = mutableListOf<Streak>()
        var failedDate: LocalDate? = null

        var previousCompletedDatePeriod: LocalDateRange? = null

        for (completedDate in completionHistory.first().date..completionHistory.last().date) {
            val completedDateIsInPreviousPeriod =
                previousCompletedDatePeriod != null && previousCompletedDatePeriod.contains(
                    completedDate
                )
            if (completedDateIsInPreviousPeriod) continue
            if (failedDate != null && failedDate > completedDate) continue

            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = completedDate,
                today = today,
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
            )

            failedDate = findNextFailedDate(
                habit = habit,
                currentDate = completedDate,
                today = today,
                maxDate = lastPossibleStreakDate,
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
                    )
                    if (todayStatus == HabitStatus.Planned) {
                        today.minus(DatePeriod(days = 1))
                    } else {
                        today
                    }
                }

                else -> lastPossibleStreakDate
            }

            val streak = Streak(startDate = streakStartDate, endDate = streakEndDate)
            val streakToMerge =
                streaks.find { it.endDate.plusDays(1) == streak.startDate }
            if (streakToMerge != null) {
                streaks.remove(streakToMerge)
                streaks.add(streak.copy(startDate = streakToMerge.startDate))
            } else {
                streaks.add(streak)
            }

            println("GetAllStreaksUseCase: completedDate=$completedDate, failedDate=$failedDate")
            previousCompletedDatePeriod = completedDatePeriod
        }

        return streaks
    }

    private fun findStreakStartDate(
        habit: Habit,
        completedDate: LocalDate,
        completedDatePeriod: LocalDateRange?,
        today: LocalDate,
    ): LocalDate {
        if (!habit.schedule.backlogEnabled) return completedDate
        val previousFailedDate = findPreviousFailedDate(
            habit = habit,
            currentDate = completedDate,
            minDate = completedDatePeriod?.start ?: habit.schedule.startDate,
            today = today,
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
    ): LocalDate? {
        for (date in currentDate..maxDate) {
            if (habit.schedule.isDue(validationDate = date)) {
                val habitStatus = habitStatusComputer.computeStatus(
                    validationDate = date,
                    today = today,
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
    ): LocalDate? {
        var date = currentDate
        while (date >= minDate) {
            if (habit.schedule.isDue(validationDate = date)) {
                val habitStatus = habitStatusComputer.computeStatus(
                    validationDate = date,
                    today = today,
                )
                if (habitStatus == HabitStatus.Failed) return date
            }
            date = date.minus(DatePeriod(days = 1))
        }
        return null
    }
}