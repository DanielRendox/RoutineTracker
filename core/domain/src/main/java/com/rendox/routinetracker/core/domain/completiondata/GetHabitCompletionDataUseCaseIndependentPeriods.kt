package com.rendox.routinetracker.core.domain.completiondata

import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.domain.habitstatus.HabitStatusComputer
import com.rendox.routinetracker.core.domain.schedule.getPeriodRange
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.model.HabitCompletionData
import com.rendox.routinetracker.core.model.Schedule
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class GetHabitCompletionDataUseCaseIndependentPeriods(
    private val getHabit: GetHabitUseCase,
    private val vacationRepository: VacationRepository,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val habitStatusComputer: HabitStatusComputer,
    private val defaultDispatcher: CoroutineContext,
) : GetHabitCompletionDataUseCase {
    override suspend operator fun invoke(
        habitId: Long,
        validationDate: LocalDate,
        today: LocalDate,
    ): HabitCompletionData = invoke(
        habitId = habitId,
        validationDates = validationDate..validationDate,
        today = today,
    ).values.first()

    override suspend operator fun invoke(
        habitId: Long,
        validationDates: LocalDateRange,
        today: LocalDate,
    ): Map<LocalDate, HabitCompletionData> = withContext(defaultDispatcher) {
        val habit = getHabit(habitId)
        val period = expandPeriodToScheduleBounds(
            requestedDates = validationDates,
            schedule = habit.schedule,
        )

        val completionHistory = if (period != null) {
            completionHistoryRepository.getRecordsInPeriod(
                habit = habit,
                minDate = period.start,
                maxDate = period.endInclusive,
            )
        } else {
            emptyList()
        }

        val vacationHistory = if (period != null) {
            vacationRepository.getVacationsInPeriod(
                habitId = habitId,
                minDate = period.start,
                maxDate = period.endInclusive,
            )
        } else {
            emptyList()
        }

        validationDates.associateWith { date ->
            val habitStatus = habitStatusComputer.computeStatus(
                validationDate = date,
                today = today,
                habit = habit,
                completionHistory = completionHistory,
                vacationHistory = vacationHistory,
            )
            val numOfTimesCompleted =
                completionHistory.find { it.date == date }?.numOfTimesCompleted ?: 0f
            HabitCompletionData(
                habitStatus = habitStatus,
                numOfTimesCompleted = numOfTimesCompleted,
            )
        }
    }

    private fun expandPeriodToScheduleBounds(
        requestedDates: LocalDateRange,
        schedule: Schedule,
    ): LocalDateRange? {
        val scheduleEndDate = schedule.endDate

        val requestedDatesLaterThanHabitEnd =
            scheduleEndDate != null && requestedDates.start > scheduleEndDate
        if (requestedDatesLaterThanHabitEnd) return null

        val requestedDatesEarlierThanHabitStart =
            requestedDates.endInclusive < schedule.startDate
        if (requestedDatesEarlierThanHabitStart) return null

        val requestedDatesIsIncorrectRange = requestedDates.endInclusive < requestedDates.start
        if (requestedDatesIsIncorrectRange) return null

        val minDate = requestedDates.start
        val requestedStart = when {
            schedule.startDate > minDate -> schedule.startDate
            else -> minDate
        }
        val schedulePeriodStart = when (schedule) {
            is Schedule.PeriodicSchedule -> schedule.getPeriodRange(requestedStart)!!.start
            else -> requestedStart
        }
        val maxDate = requestedDates.endInclusive
        val requestedEnd = when {
            scheduleEndDate != null && scheduleEndDate < maxDate ->
                scheduleEndDate
            else -> maxDate
        }
        val schedulePeriodEnd = when (schedule) {
            is Schedule.PeriodicSchedule -> schedule.getPeriodRange(requestedEnd)!!.endInclusive
            else -> requestedEnd
        }
        return schedulePeriodStart..schedulePeriodEnd
    }
}