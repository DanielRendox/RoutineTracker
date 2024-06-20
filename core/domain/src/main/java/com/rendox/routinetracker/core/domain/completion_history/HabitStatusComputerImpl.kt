package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

internal class HabitStatusComputerImpl : HabitStatusComputer {

    /**
     * The HabitComputeStatusUseCase class is responsible for computing the [HabitStatus] based on
     * various factors such as the habit's schedule, what dates are completed, and vacation periods.
     *
     * The result also depends on whether the validation date is in the past or in the future. For
     * example, when the habit has some backlog, but the validation date is in the past, the invoke
     * function will return not [HabitStatus.Backlog], but [HabitStatus.NotDue] instead. That's because
     * the user deliberately chose to skip the habit on that day. Nonetheless, if the validation date is
     * in the future, the invoke function will return [HabitStatus.Backlog] so that the user can adjust
     * their schedule to sort out this backlog later.
     *
     * Note that today is considered to be in the future except cases with skipped and already
     * completed statuses because the user should visually see that today is included in streak.
     *
     * When the habit is on vacation, it's considered to be not due even if it's planned on schedule.
     * During the vacation, the user can still completed the habit, which will either sort out the
     * backlog (if any is present), or will complete the habit ahead.
     *
     * Each date depends on whether other dates are completed or not. For example, when one date has
     * status [HabitStatus.OverCompleted], the user will be able to skip the next planned date, which
     * will have already completed status. In the case of the previous example, with
     * backlog, the next over completed date will have status [HabitStatus.SortedOutBacklog].
     *
     * Backlog and completing ahead can be disabled by toggling the [Habit]'s [Schedule]'s properties.
     *
     * Period separation can be enabled or disabled as well. If enabled, the schedule deviation that
     * indicates backlog and how many times the habit was completed ahead will be reset at the start
     * of each period.
     *
     * @see [HabitStatus] for more details on what each status means, and when it is returned.
     */
    override fun computeStatus(
        validationDate: LocalDate,
        today: LocalDate,
        habit: Habit,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): HabitStatus {
        if (validationDate < habit.schedule.startDate) return HabitStatus.NotStarted
        habit.schedule.endDate?.let { if (validationDate > it) return HabitStatus.Finished }

        val numOfTimesCompleted = completionHistory
            .find { it.date == validationDate }
            ?.numOfTimesCompleted ?: 0f

        val scheduleDeviation by lazy {
            computeScheduleDeviation(
                habit = habit,
                currentDate = validationDate,
                today = today,
                completionHistory = completionHistory,
                vacationHistory = vacationHistory,
            )
        }
        val completedToday = completionHistory.find { it.date == today } != null
        val hasBacklog by lazy {
            checkIfHasBacklog(
                habit = habit,
                scheduleDeviation = scheduleDeviation,
                validationDate = validationDate,
                today = today,
                completedToday = completedToday,
                vacationHistory = vacationHistory,
            )
        }

        val isOnVacation = vacationHistory.any { it.containsDate(validationDate) }
        if (isOnVacation) {
            return if (numOfTimesCompleted > 0f) {
                if (hasBacklog) HabitStatus.SortedOutBacklog else HabitStatus.OverCompleted
            } else {
                HabitStatus.OnVacation
            }
        }

        val numOfDueTimes = habit.getNumOfDueTimesOnDate(
            date = validationDate,
            habitIsOnVacation = false,
        )

        if (numOfTimesCompleted > 0f) {
            return when {
                numOfTimesCompleted == numOfDueTimes -> HabitStatus.Completed
                numOfTimesCompleted < numOfDueTimes -> HabitStatus.PartiallyCompleted
                else -> if (hasBacklog) HabitStatus.SortedOutBacklog else HabitStatus.OverCompleted
            }
        }

        if (numOfDueTimes <= 0f) {
            return if (validationDate >= today && hasBacklog) {
                HabitStatus.Backlog
            } else {
                HabitStatus.NotDue
            }
        }

        val isAlreadyCompleted = checkIfIsAlreadyCompleted(
            habit = habit,
            scheduleDeviation = scheduleDeviation,
            numOfDueTimesOnValidationDate = numOfDueTimes,
            validationDate = validationDate,
            today = today,
            completedToday = completedToday,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )
        if (isAlreadyCompleted) return HabitStatus.AlreadyCompleted

        if (validationDate >= today) return HabitStatus.Planned

        val isCompletedLater = checkIfWasCompletedLater(
            habit = habit,
            currentDate = validationDate,
            numOfDueTimesOnCurrentDate = numOfDueTimes,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
        )
        return if (isCompletedLater) HabitStatus.CompletedLater else HabitStatus.Failed
    }

    private fun checkIfHasBacklog(
        habit: Habit,
        scheduleDeviation: Double,
        validationDate: LocalDate,
        today: LocalDate,
        completedToday: Boolean,
        vacationHistory: List<Vacation>,
    ): Boolean {
        if (scheduleDeviation < 0.0 && habit.schedule.backlogEnabled) {
            val numOfNotDueTimes = if (validationDate >= today) {
                val startDate = if (completedToday) today.plusDays(1) else today
                getNumOfNotDueTimesInPeriod(
                    habit = habit,
                    period = startDate..validationDate,
                    vacationHistory = vacationHistory,
                )
            } else 0.0
            if (scheduleDeviation <= -numOfNotDueTimes) return true
        }
        return false
    }

    private fun getNumOfNotDueTimesInPeriod(
        habit: Habit,
        period: LocalDateRange,
        vacationHistory: List<Vacation>,
    ): Double {
        var numOfNotDueTimesInPeriod = 0.0

        val defaultNumOfDueTimesOnDate = when (habit) {
            is Habit.YesNoHabit -> 1F
        }

        for (date in period) {
            val habitIsOnVacation = vacationHistory.any { it.containsDate(date) }
            val habitIsDue = if (habitIsOnVacation) {
                false
            } else {
                habit.schedule.isDue(validationDate = date)
            }
            if (!habitIsDue) numOfNotDueTimesInPeriod += defaultNumOfDueTimesOnDate
        }

        return numOfNotDueTimesInPeriod
    }

    private fun checkIfIsAlreadyCompleted(
        habit: Habit,
        scheduleDeviation: Double,
        numOfDueTimesOnValidationDate: Float,
        validationDate: LocalDate,
        today: LocalDate,
        completedToday: Boolean,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): Boolean {
        if (!habit.schedule.completingAheadEnabled) return false

        if (habit.schedule.backlogEnabled) {
            val numOfDueTimes =
                if (validationDate >= today) {
                    val actualDate = if (completedToday) {
                        today.plusDays(1)
                    } else {
                        today
                    }
                    getNumOfDueTimesInPeriod(
                        habit = habit,
                        period = actualDate..validationDate,
                        vacationHistory = vacationHistory,
                    )
                } else {
                    numOfDueTimesOnValidationDate.toDouble()
                }
            if (scheduleDeviation >= numOfDueTimes) {
                return true
            }
        } else {
            // when backlog is disabled, there may be a situation when the schedule deviation is
            // negative and the user can neither sort out the backlog nor complete ahead; the
            // following code is required to fix this bug
            val schedule = habit.schedule
            val currentDatePeriod: LocalDateRange? =
                if (schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled) {
                    schedule.getPeriodRange(currentDate = validationDate)
                } else {
                    null
                }

            val firstDateInPeriod = currentDatePeriod?.start
            val firstDateToLookFor = completionHistory.filter {
                val dateIsLaterThanFirstDateInPeriod =
                    firstDateInPeriod == null || firstDateInPeriod <= it.date
                val dateIsEarlierThanValidationDate = it.date < validationDate
                dateIsLaterThanFirstDateInPeriod && dateIsEarlierThanValidationDate
            }.minOfOrNull { it.date } ?: return false
            val lastDateToLookFor = validationDate.minus(DatePeriod(days = 1))

            var numOfDueTimes = 0.0
            var numOfTimesCompleted = 0.0

            var date = lastDateToLookFor
            while (date >= firstDateToLookFor) {
                numOfDueTimes += habit.getNumOfDueTimesOnDate(
                    date = date,
                    habitIsOnVacation = vacationHistory.any { it.containsDate(date) },
                )
                numOfTimesCompleted +=
                    completionHistory.find { it.date == date }?.numOfTimesCompleted ?: 0f

                if (numOfTimesCompleted - numOfDueTimes >= numOfDueTimesOnValidationDate) {
                    return true
                }

                date = date.minus(DatePeriod(days = 1))
            }
        }
        return false
    }

    /**
     * @return positive value if the habit is ahead of schedule (completed even more than planned),
     * negative if behind (there is some backlog), 0 if on schedule
     */
    private fun computeScheduleDeviation(
        habit: Habit,
        today: LocalDate,
        currentDate: LocalDate,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): Double {
        val actualDate = if (currentDate <= today) {
            currentDate.minus(DatePeriod(days = 1))
        } else {
            val completedToday = completionHistory.any { it.date == today }
            if (completedToday) {
                today
            } else {
                today.minus(DatePeriod(days = 1))
            }
        }

        val schedule = habit.schedule
        val period =
            if (schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled) {
                val lastPeriod = schedule.getPeriodRange(currentDate = actualDate)
                if (lastPeriod == null || currentDate !in lastPeriod) return 0.0
                lastPeriod.start..actualDate
            } else {
                schedule.startDate..actualDate
            }

        val numOfTimesCompleted = completionHistory
            .filter { it.date in period }
            .sumOf { it.numOfTimesCompleted.toDouble() }
        val numOfDueTimes = getNumOfDueTimesInPeriod(habit, period, vacationHistory)
        return numOfTimesCompleted - numOfDueTimes
    }

    /**
     * @return true if the habit wasn't completed on the date it was planned and introduced a
     * backlog that was sorted out later
     */
    private fun checkIfWasCompletedLater(
        habit: Habit,
        currentDate: LocalDate,
        numOfDueTimesOnCurrentDate: Float,
        completionHistory: List<Habit.CompletionRecord>,
        vacationHistory: List<Vacation>,
    ): Boolean {
        if (!habit.schedule.backlogEnabled) return false

        val schedule = habit.schedule
        val currentDatePeriod: LocalDateRange? =
            if (schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled) {
                schedule.getPeriodRange(currentDate = currentDate)
            } else {
                null
            }

        val lastCompletedDate =
            completionHistory.maxOfOrNull { it.date } ?: return false
        val lastDateInPeriod = currentDatePeriod?.endInclusive
        val firstDateToLookFor = currentDate.plusDays(1)
        val lastDateToLookFor =
            if (lastDateInPeriod != null && lastDateInPeriod < lastCompletedDate) {
                lastDateInPeriod
            } else {
                lastCompletedDate
            }

        var numOfDueTimes = 0.0
        var numOfTimesCompleted = 0.0

        for (date in firstDateToLookFor..lastDateToLookFor) {
            numOfDueTimes += habit.getNumOfDueTimesOnDate(
                date = date,
                habitIsOnVacation = vacationHistory.any { it.containsDate(date) },
            )
            numOfTimesCompleted +=
                completionHistory.find { it.date == date }?.numOfTimesCompleted ?: 0f

            val scheduleDeviation = numOfTimesCompleted - numOfDueTimes
            if (scheduleDeviation >= numOfDueTimesOnCurrentDate) return true
        }
        return false
    }

    private fun getNumOfDueTimesInPeriod(
        habit: Habit,
        period: LocalDateRange,
        vacationHistory: List<Vacation>,
    ): Double {
        var numOfDueTimesInPeriod = 0.0

        for (date in period) {
            numOfDueTimesInPeriod += habit.getNumOfDueTimesOnDate(
                date = date,
                habitIsOnVacation = vacationHistory.any { it.containsDate(date) },
            )
        }

        return numOfDueTimesInPeriod
    }

    private fun Habit.getNumOfDueTimesOnDate(
        date: LocalDate,
        habitIsOnVacation: Boolean,
    ): Float {
        val numOfDueTimes = when (this) {
            is Habit.YesNoHabit -> {
                val dueOnSchedule = if (habitIsOnVacation) {
                    false
                } else {
                    schedule.isDue(validationDate = date)
                }
                if (dueOnSchedule) 1f else 0f
            }
        }
        return numOfDueTimes
    }
}