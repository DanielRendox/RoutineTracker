package com.rendox.routinetracker.core.domain.completionhistory

import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.domain.completionhistory.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.domain.streak.StreakManager
import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

/**
 * Besides inserting a completion record, this use case also computes and cashes the streaks that have
 * been created as a result of completing the habit.
 */
class InsertHabitCompletionAndCashStreaks(
    private val completionHistoryRepository: CompletionHistoryRepository,
    private val getHabit: GetHabitUseCase,
    private val streakManager: StreakManager,
) : InsertHabitCompletionUseCase {
    /**
     * @throws InsertHabitCompletionUseCase.IllegalDateEditAttemptException if the [completionRecord]'s
     * date is earlier than the habit's start date or later than the habit's end date (if it
     * exists), or if the date is later than today.
     */
    override suspend fun invoke(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
        today: LocalDate,
    ) {
        val habit = getHabit(habitId)
        if (completionRecord.date < habit.schedule.startDate) {
            throw IllegalDateEditAttemptException.NotStartedHabitDateEditAttemptException()
        }
        habit.schedule.endDate?.let { endDate ->
            if (completionRecord.date > endDate) {
                throw IllegalDateEditAttemptException.FinishedHabitDateEditAttemptException()
            }
        }
        if (completionRecord.date > today) {
            throw IllegalDateEditAttemptException.FutureDateEditAttemptException()
        }

        val periodToStreaks = streakManager.formStreaks(
            habit = habit,
            today = today,
            completion = completionRecord,
        )
        if (periodToStreaks != null) {
            completionHistoryRepository.insertCompletionAndCacheStreaks(
                habitId = habitId,
                completionRecord = completionRecord,
                streaks = periodToStreaks.second,
                period = periodToStreaks.first,
            )
        } else {
            completionHistoryRepository.insertCompletion(habitId, completionRecord)
        }
    }
}