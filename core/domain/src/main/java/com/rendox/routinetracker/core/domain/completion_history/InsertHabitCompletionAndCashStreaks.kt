package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.domain.streak.StreakComputer
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

/**
 * Besides inserting a completion record, this use case also computes and cashes streaks for the
 * period that the completion record belongs to.
 *
 * @see InsertHabitCompletionUseCaseImpl
 */
class InsertHabitCompletionAndCashStreaks(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val vacationRepository: VacationRepository,
    private val getHabit: GetHabitUseCase,
    private val streakComputer: StreakComputer,
    private val streakRepository: StreakRepository,
) : InsertHabitCompletionUseCaseImpl(
    completionHistoryRepository = completionHistoryRepository,
    getHabit = getHabit,
) {
    /**
     * @throws InsertHabitCompletionUseCaseImpl.IllegalDateException if the [completionRecord]'s
     * date is earlier than the habit's start date or later than the habit's end date (if it
     * exists), or if the date is later than today.
     */
    override suspend fun invoke(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
        today: LocalDate
    ) {
        super.invoke(habitId, completionRecord, today)
        cashStreaks(
            habit = getHabit(habitId),
            completedDate = completionRecord.date,
            today = today,
        )
    }

    private suspend fun cashStreaks(
        habit: Habit,
        completedDate: LocalDate,
        today: LocalDate,
    ) {
        val cashedPeriod = streakRepository.getCashedPeriod(
            habitId = habit.id!!,
            dateInPeriod = completedDate,
        ) ?: return

        val completionHistory = completionHistoryRepository.getRecordsInPeriod(
            habitId = habit.id!!,
            minDate = cashedPeriod.start,
            maxDate = cashedPeriod.endInclusive,
        )
        val vacationHistory = vacationRepository.getVacationsInPeriod(
            habitId = habit.id!!,
            minDate = cashedPeriod.start,
            maxDate = cashedPeriod.endInclusive,
        )
        val streaks = streakComputer.computeStreaksInPeriod(
            period = cashedPeriod,
            habit = habit,
            completionHistory = completionHistory,
            vacationHistory = vacationHistory,
            today = today,
        )

        streakRepository.deleteStreaksInPeriod(
            habitId = habit.id!!,
            periodStartDate = cashedPeriod.start,
            periodEndDate = cashedPeriod.endInclusive,
        )
        streakRepository.insertStreaks(
            streaks = streaks.map { habit.id!! to it },
            periods = listOf(habit.id!! to cashedPeriod),
        )
    }
}