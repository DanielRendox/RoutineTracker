package com.rendox.routinetracker.core.domain.streak

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.schedule.getPeriodRange
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.logic.time.atEndOfMonth
import com.rendox.routinetracker.core.logic.time.isSubsetOf
import com.rendox.routinetracker.core.logic.time.plusDays
import com.rendox.routinetracker.core.logic.time.rangeTo
import com.rendox.routinetracker.core.logic.time.withDayOfMonth
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.Streak
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class GetAllStreaksWithCashingUseCase(
    private val getHabit: GetHabitUseCase,
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val vacationHistoryRepository: VacationRepository,
    private val defaultDispatcher: CoroutineContext,
    private val streakRepository: StreakRepository,
    private val streakComputer: StreakComputer,
) : GetAllStreaksUseCase {
    override suspend operator fun invoke(
        habitId: Long,
        today: LocalDate,
    ): List<Streak> = withContext(defaultDispatcher) {
        val habit = getHabit(habitId)
        if (habit.schedule is Schedule.CustomDateSchedule) {
            return@withContext emptyList<Streak>()
        }

        val cashedStreaks = streakRepository.getAllStreaks(habitId)
        val cashedPeriods = streakRepository.getAllCashedPeriods(habitId)

        val computedStreaks = mutableListOf<Streak>()
        val computedPeriods = mutableListOf<LocalDateRange>()

        val firstDateToLookFor = habit.schedule.startDate
        var period = getPeriodRange(habit.schedule, firstDateToLookFor)!!

        if (period.start > today) return@withContext emptyList<Streak>()

        while (!period.contains(today)) {
            val cashedStreaksContainsPeriod = cashedPeriods.any { cashedPeriod ->
                period.isSubsetOf(cashedPeriod)
            }
            if (cashedStreaksContainsPeriod) {
                period = getPeriodRange(habit.schedule, period.endInclusive.plusDays(1)) ?: break
                continue
            }
            computedStreaks.addAll(getStreaksInPeriod(habit, period, today))
            computedPeriods.add(period)
            period = getPeriodRange(habit.schedule, period.endInclusive.plusDays(1)) ?: break
        }
        val distinctComputedStreaks = computedStreaks.distinct()
        val actuallyNeedToInsertComputedStreaks = computedPeriods.isNotEmpty()
        if (actuallyNeedToInsertComputedStreaks) {
            streakRepository.insertStreaks(
                streaks = distinctComputedStreaks.map { habitId to it },
                periods = computedPeriods.map { habitId to it },
            )
        }

        // don't insert current period streaks, because they may be incomplete and inserting
        // such ambiguous data may result in incorrect streak computation in the future
        val currentPeriodStreaks = getStreaksInPeriod(habit, period, today)

        (distinctComputedStreaks + cashedStreaks + currentPeriodStreaks)
            .distinct()
            .joinAdjacentStreaks()
    }

    private suspend fun getStreaksInPeriod(
        habit: Habit,
        period: LocalDateRange,
        today: LocalDate,
    ): List<Streak> {
        val completionHistory = completionHistoryRepository.getRecordsInPeriod(
            habit = habit,
            minDate = period.start,
            maxDate = period.endInclusive,
        )
        val vacationHistory = vacationHistoryRepository.getVacationsInPeriod(
            habitId = habit.id!!,
            minDate = period.start,
            maxDate = period.endInclusive,
        )
        return streakComputer.computeStreaksInPeriod(
            today = today,
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            period = period,
        )
    }

    companion object {
        private fun getPeriodRange(
            schedule: Schedule,
            currentDate: LocalDate,
        ): LocalDateRange? = when (schedule) {
            is Schedule.PeriodicSchedule -> schedule.getPeriodRange(currentDate)
            is Schedule.EveryDaySchedule -> currentDate.withDayOfMonth(1)..currentDate.atEndOfMonth
            is Schedule.CustomDateSchedule -> throw IllegalArgumentException()
        }
    }
}