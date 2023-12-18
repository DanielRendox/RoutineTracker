package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.domain.completion_history.schedule.isDue
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class HabitComputeStatusUseCase(
    private val habitIsOnVacationOnDateProvider: suspend (LocalDate) -> Boolean,
    private val lastVacationEndDateBeforeDateProvider: suspend (LocalDate) -> LocalDate?,
    private val numOfTimesCompletedInPeriodProvider: suspend (LocalDateRange) -> Double,
    private val numOfTimesCompletedOnDateProvider: suspend (LocalDate) -> Float,
    private val lastCompletedDateProvider: suspend () -> LocalDate?,
) {

    suspend operator fun invoke(
        habit: Habit,
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitStatus {
        if (validationDate < habit.schedule.startDate) return HabitStatus.NotStarted
        habit.schedule.endDate?.let { if (validationDate > it) return HabitStatus.Finished }

        val lastVacationEndDateBeforeValidationDate =
            lastVacationEndDateBeforeDateProvider(validationDate)

        val scheduleDeviation = computeScheduleDeviation(
            currentDate = if (validationDate < today) validationDate else today,
            habit = habit,
        )
        val numOfTimesCompletedOnValidationDate = numOfTimesCompletedOnDateProvider(validationDate)

        val habitIsOnVacationAtTheMomentOfValidationDate =
            habitIsOnVacationOnDateProvider(validationDate)
        if (habitIsOnVacationAtTheMomentOfValidationDate) {
            if (numOfTimesCompletedOnValidationDate > 0f) {
                if (scheduleDeviation < 0.0 && habit.schedule.backlogEnabled) {
                    return HabitStatus.SortedOutBacklog
                }
                return HabitStatus.OverCompleted
            }
            return HabitStatus.OnVacation
        }

        val numOfDueTimesOnValidationDate = habit.getNumOfDueTimesOnDate(
            date = validationDate,
            lastVacationEndDate = lastVacationEndDateBeforeValidationDate,
        )

        val validationDateIsDue = numOfDueTimesOnValidationDate > 0f
        if (validationDateIsDue) {
            val completedStatus = deriveCompletedStatusWhenPlanned(
                habit = habit,
                scheduleDeviation = scheduleDeviation,
                numOfTimesCompletedOnValidationDate = numOfTimesCompletedOnValidationDate,
                numOfDueTimesOnValidationDate = numOfDueTimesOnValidationDate,
            )
            if (completedStatus != null) return completedStatus

            val alreadyCompleted = checkIfIsAlreadyCompleted(
                habit, scheduleDeviation, numOfDueTimesOnValidationDate, validationDate, today
            )
            if (alreadyCompleted) return HabitStatus.AlreadyCompleted

            if (validationDate < today) {
                val wasCompletedLater = checkIfWasCompletedLater(
                    currentDate = validationDate,
                    habit = habit,
                    lastVacationEndDate = lastVacationEndDateBeforeValidationDate,
                )
                if (wasCompletedLater) return HabitStatus.CompletedLater
            }

            return if (validationDate < today) HabitStatus.Failed else HabitStatus.Planned
        } else {
            val backlogStatus = deriveBacklogStatus(
                habit, scheduleDeviation, numOfTimesCompletedOnValidationDate, validationDate, today
            )
            if (backlogStatus != null) return backlogStatus

            if (numOfTimesCompletedOnValidationDate > 0f) {
                return HabitStatus.OverCompleted
            }
            return HabitStatus.NotDue
        }
    }

    private fun deriveCompletedStatusWhenPlanned(
        habit: Habit,
        scheduleDeviation: Double,
        numOfTimesCompletedOnValidationDate: Float,
        numOfDueTimesOnValidationDate: Float,
    ): HabitStatus? = when {
        numOfTimesCompletedOnValidationDate == numOfDueTimesOnValidationDate ->
            HabitStatus.Completed

        numOfTimesCompletedOnValidationDate > numOfDueTimesOnValidationDate -> {
            if (scheduleDeviation < 0.0 && habit.schedule.backlogEnabled) {
                HabitStatus.SortedOutBacklog
            } else {
                HabitStatus.OverCompleted
            }
        }

        numOfTimesCompletedOnValidationDate > 0f -> HabitStatus.PartiallyCompleted
        else -> null
    }

    private suspend fun deriveBacklogStatus(
        habit: Habit,
        scheduleDeviation: Double,
        numOfTimesCompletedOnValidationDate: Float,
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitStatus? {
        if (scheduleDeviation < 0.0 && habit.schedule.backlogEnabled) {
            val numOfNotDueTimes =
                if (validationDate >= today) {
                    getNumOfDueAndNotDueTimesInPeriod(
                        habit = habit,
                        period = today..validationDate,
                    ).first
                } else {
                    0.0
                }
            if (scheduleDeviation <= -numOfNotDueTimes) {
                if (numOfTimesCompletedOnValidationDate > 0f) {
                    return HabitStatus.SortedOutBacklog
                }
                if (validationDate >= today) {
                    return HabitStatus.Backlog
                }
            }
        }
        return null
    }

    private suspend fun checkIfIsAlreadyCompleted(
        habit: Habit,
        scheduleDeviation: Double,
        numOfDueTimesOnValidationDate: Float,
        validationDate: LocalDate,
        today: LocalDate,
    ): Boolean {
        if (scheduleDeviation >= numOfDueTimesOnValidationDate && habit.schedule.completingAheadEnabled) {
            val numOfDueTimes =
                if (validationDate >= today) {
                    getNumOfDueAndNotDueTimesInPeriod(
                        habit = habit,
                        period = today..validationDate,
                    ).first
                } else {
                    numOfDueTimesOnValidationDate.toDouble()
                }
            if (scheduleDeviation >= numOfDueTimes) {
                return true
            }
        }
        return false
    }

    private suspend fun computeScheduleDeviation(
        habit: Habit,
        currentDate: LocalDate
    ): Double {
        val schedule = habit.schedule
        return if (schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled) {
            val lastPeriod = schedule.getPeriodRange(
                currentDate = currentDate.minus(DatePeriod(days = 1)),
                lastVacationEndDate = lastVacationEndDateBeforeDateProvider(
                    currentDate.minus(DatePeriod(days = 1))
                ),
            )
            if (lastPeriod != null && currentDate in lastPeriod) {
                val period = lastPeriod.start..currentDate.minus(DatePeriod(days = 1))
                val numOfTimesCompleted = numOfTimesCompletedInPeriodProvider(period)
                val numOfDueTimes = getNumOfDueAndNotDueTimesInPeriod(habit, period).first
                numOfTimesCompleted - numOfDueTimes
            } else {
                0.0
            }
        } else {
            val period = schedule.startDate..currentDate.minus(DatePeriod(days = 1))
            val numOfTimesCompleted = numOfTimesCompletedInPeriodProvider(period)
            val numOfDueTimes = getNumOfDueAndNotDueTimesInPeriod(habit, period).first
            numOfTimesCompleted - numOfDueTimes
        }
    }

    private suspend fun checkIfWasCompletedLater(
        habit: Habit,
        currentDate: LocalDate,
        lastVacationEndDate: LocalDate?,
    ): Boolean {
        val schedule = habit.schedule
        val currentDatePeriod: LocalDateRange? =
            if (schedule is Schedule.PeriodicSchedule) schedule.getPeriodRange(
                currentDate = currentDate,
                lastVacationEndDate = lastVacationEndDate,
            ) else null

        val lastCompletedDate = lastCompletedDateProvider() ?: return false
        val lastDateInPeriod = currentDatePeriod?.endInclusive

        val firstDateToLookFor = currentDate.plusDays(1)
        val lastDateToLookFor =
            if (lastDateInPeriod != null && lastDateInPeriod < lastCompletedDate) {
                lastDateInPeriod
            } else {
                lastCompletedDate
            }

        var numOfDueTimes = 0.0
        val numOfTimesCompleted =
            numOfTimesCompletedInPeriodProvider(firstDateToLookFor..lastDateToLookFor)
        val numOfDueTimesOnCurrentDate = habit.getNumOfDueTimesOnDate(
            date = currentDate,
            lastVacationEndDate = lastVacationEndDate,
        )

        for (date in firstDateToLookFor..lastDateToLookFor) {
            numOfDueTimes += habit.getNumOfDueTimesOnDate(
                date = date,
                lastVacationEndDate = lastVacationEndDate,
            )
            val scheduleDeviation = numOfTimesCompleted - numOfDueTimes

            if (scheduleDeviation >= numOfDueTimesOnCurrentDate) return true
        }
        return false
    }

    private suspend fun getNumOfDueAndNotDueTimesInPeriod(
        habit: Habit,
        period: LocalDateRange
    ): Pair<Double, Double> {
        var numOfDueTimesInPeriod = 0.0
        var numOfNotDueTimesInPeriod = 0.0

        for (date in period) {
            val dateIsDue = habit.schedule.isDue(
                validationDate = date,
                lastVacationEndDate = lastVacationEndDateBeforeDateProvider(date),
            )
            if (dateIsDue) {
                numOfDueTimesInPeriod += habit.getDefaultNumOfDueTimes()
            } else {
                numOfNotDueTimesInPeriod += habit.getDefaultNumOfDueTimes()
            }
        }

        return numOfDueTimesInPeriod to numOfNotDueTimesInPeriod
    }

    private fun Habit.getNumOfDueTimesOnDate(
        date: LocalDate?,
        lastVacationEndDate: LocalDate?,
    ): Float = when (this) {
        is Habit.YesNoHabit -> {
            date?.let {
                val dueOnSchedule = schedule.isDue(
                    validationDate = it,
                    lastVacationEndDate = lastVacationEndDate,
                )
                if (dueOnSchedule) 1f else 0f
            } ?: 1f
        }
    }

    private fun Habit.getDefaultNumOfDueTimes(): Float = when (this) {
        is Habit.YesNoHabit -> 1f
    }
}
