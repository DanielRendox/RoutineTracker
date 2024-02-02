package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.model.Habit
import kotlinx.datetime.LocalDate

interface InsertHabitCompletionUseCase {
    suspend operator fun invoke(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
        today: LocalDate,
    )

    sealed class IllegalDateEditAttemptException : Exception() {
        object FutureDateEditAttemptException : IllegalDateEditAttemptException()
        object FinishedHabitDateEditAttemptException : IllegalDateEditAttemptException()
        object NotStartedHabitDateEditAttemptException : IllegalDateEditAttemptException()
    }
}