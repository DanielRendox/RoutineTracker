package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
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

class HabitComputeStatusUseCase(
    private val habitRepository: HabitRepository,
    private val vacationRepository: VacationRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
) {

    // TODO switch threads for CPU-intensive tasks

    suspend operator fun invoke(
        habitId: Long,
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitStatus {
        val habit = habitRepository.getHabitById(habitId)

        if (validationDate < habit.schedule.startDate) return HabitStatus.NotStarted
        habit.schedule.endDate?.let { if (validationDate > it) return HabitStatus.Finished }

        val scheduleDeviation = computeScheduleDeviation(
            habit = habit,
            currentDate = validationDate,
            today = today,
        )

        val numOfTimesCompletedOnValidationDate = completionHistoryRepository.getRecordByDate(
            habitId = habit.id!!,
            date = validationDate,
        )?.numOfTimesCompleted ?: 0f

        val habitIsOnVacationAtTheMomentOfValidationDate = vacationRepository.getVacationByDate(
            habitId = habit.id!!,
            date = validationDate,
        ) != null

        val numOfDueTimesOnValidationDate = habit.getNumOfDueTimesOnDate(
            date = validationDate, habitIsOnVacation = habitIsOnVacationAtTheMomentOfValidationDate
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
                    numOfDueTimesOnCurrentDate = numOfDueTimesOnValidationDate,
                    habit = habit,
                )
                if (wasCompletedLater) return HabitStatus.CompletedLater
            }

            return if (validationDate < today) HabitStatus.Failed else HabitStatus.Planned
        } else {
            val backlogStatus = deriveBacklogStatus(
                habit,
                scheduleDeviation,
                numOfTimesCompletedOnValidationDate,
                validationDate,
                today
            )
            if (backlogStatus != null) return backlogStatus

            if (numOfTimesCompletedOnValidationDate > 0f) {
                return HabitStatus.OverCompleted
            }
            if (habitIsOnVacationAtTheMomentOfValidationDate) return HabitStatus.OnVacation
            return if (validationDate < today) HabitStatus.Skipped else HabitStatus.NotDue
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
                    getNumOfNotDueTimesInPeriod(
                        habit = habit,
                        period = today..validationDate,
                    )
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

    private suspend fun getNumOfNotDueTimesInPeriod(
        habit: Habit,
        period: LocalDateRange,
    ): Double {
        var numOfNotDueTimesInPeriod = 0.0

        val defaultNumOfDueTimesOnDate = when (habit) {
            is Habit.YesNoHabit -> 1F
        }

        val vacations: List<Vacation> = vacationRepository.getVacationsInPeriod(
            habitId = habit.id!!,
            minDate = period.start,
            maxDate = period.endInclusive,
        )

        for (date in period) {
            val habitIsOnVacation = vacations.any { it.containsDate(date) }
            val habitIsDue = if (habitIsOnVacation) {
                false
            } else {
                habit.schedule.isDue(validationDate = date)
            }
            if (!habitIsDue) numOfNotDueTimesInPeriod += defaultNumOfDueTimesOnDate
        }

        return numOfNotDueTimesInPeriod
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
                    getNumOfDueTimesInPeriod(
                        habit = habit,
                        period = today..validationDate,
                    )
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
        today: LocalDate,
        currentDate: LocalDate
    ): Double {
        val actualDate = if (currentDate <= today) {
            currentDate.minus(DatePeriod(days = 1))
        } else {
            if (completionHistoryRepository.getRecordByDate(habit.id!!, today) != null) {
                today
            } else {
                today.minus(DatePeriod(days = 1))
            }
        }

        val schedule = habit.schedule
        return if (schedule is Schedule.PeriodicSchedule && schedule.periodSeparationEnabled) {
            val lastPeriod = schedule.getPeriodRange(currentDate = actualDate)
            if (lastPeriod != null && currentDate in lastPeriod) {
                val period = lastPeriod.start..actualDate
                val numOfTimesCompleted =
                    completionHistoryRepository.getNumOfTimesCompletedInPeriod(
                        habitId = habit.id!!,
                        minDate = period.start,
                        maxDate = period.endInclusive,
                    )
                val numOfDueTimes = getNumOfDueTimesInPeriod(habit, period)
                numOfTimesCompleted - numOfDueTimes
            } else {
                0.0
            }
        } else {
            val period = schedule.startDate..actualDate
            val numOfTimesCompleted = completionHistoryRepository.getNumOfTimesCompletedInPeriod(
                habitId = habit.id!!,
                minDate = period.start,
                maxDate = period.endInclusive,
            )
            val numOfDueTimes = getNumOfDueTimesInPeriod(habit, period)
            numOfTimesCompleted - numOfDueTimes
        }
    }

    private suspend fun checkIfWasCompletedLater(
        habit: Habit,
        currentDate: LocalDate,
        numOfDueTimesOnCurrentDate: Float,
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
            completionHistoryRepository.getLastCompletedRecord(habit.id!!)?.date ?: return false
        val lastDateInPeriod = currentDatePeriod?.endInclusive

        val firstDateToLookFor = currentDate.plusDays(1)
        val lastDateToLookFor =
            if (lastDateInPeriod != null && lastDateInPeriod < lastCompletedDate) {
                lastDateInPeriod
            } else {
                lastCompletedDate
            }

        val completionRecords: List<Habit.CompletionRecord> =
            completionHistoryRepository.getRecordsInPeriod(
                habitId = habit.id!!,
                minDate = firstDateToLookFor,
                maxDate = lastDateToLookFor,
            )
        val vacations: List<Vacation> = vacationRepository.getVacationsInPeriod(
            habitId = habit.id!!,
            minDate = firstDateToLookFor,
            maxDate = lastDateToLookFor,
        )

        var numOfDueTimes = 0.0
        var numOfTimesCompleted = 0.0

        for (date in firstDateToLookFor..lastDateToLookFor) {
            numOfDueTimes += habit.getNumOfDueTimesOnDate(
                date = date,
                habitIsOnVacation = vacations.any { it.containsDate(date) },
            )
           numOfTimesCompleted +=
               completionRecords.find { it.date == date }?.numOfTimesCompleted ?: 0f

            val scheduleDeviation = numOfTimesCompleted - numOfDueTimes
            if (scheduleDeviation >= numOfDueTimesOnCurrentDate) return true
        }
        return false
    }

    private suspend fun getNumOfDueTimesInPeriod(
        habit: Habit,
        period: LocalDateRange,
    ): Double {
        var numOfDueTimesInPeriod = 0.0

        val vacationsInPeriod = vacationRepository.getVacationsInPeriod(
            habitId = habit.id!!,
            minDate = period.start,
            maxDate = period.endInclusive,
        )

        for (date in period) {
            numOfDueTimesInPeriod += habit.getNumOfDueTimesOnDate(
                date = date,
                habitIsOnVacation = vacationsInPeriod.any { it.containsDate(date) },
            )
        }

        return numOfDueTimesInPeriod
    }

    private fun Habit.getNumOfDueTimesOnDate(
        date: LocalDate,
        habitIsOnVacation: Boolean,
    ): Float = when (this) {
        is Habit.YesNoHabit -> {
            val dueOnSchedule = if (habitIsOnVacation) {
                false
            } else {
                schedule.isDue(validationDate = date)
            }
            if (dueOnSchedule) 1f else 0f
        }
    }
}
