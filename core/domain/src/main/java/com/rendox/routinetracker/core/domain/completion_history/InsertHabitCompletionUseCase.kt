package com.rendox.routinetracker.core.domain.completion_history

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.model.Habit

class InsertHabitCompletionUseCase(
    private val completionHistoryRepository: CompletionHistoryRepository
) {

    suspend operator fun invoke(
        habitId: Long,
        completionRecord: Habit.CompletionRecord,
    ) {
        if (completionRecord.numOfTimesCompleted == 0F) {
            completionHistoryRepository.deleteCompletionByDate(
                habitId = habitId,
                date = completionRecord.date,
            )
        } else {
            completionHistoryRepository.insertCompletion(
                habitId = habitId,
                completionRecord = completionRecord,
            )
        }
    }
}