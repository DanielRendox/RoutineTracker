package com.rendox.routinetracker.core.database.model

import com.rendox.routinetracker.core.database.CompletionHistoryEntity
import com.rendox.routinetracker.core.model.Habit

fun CompletionHistoryEntity.toExternalModel(habit: Habit): Habit.CompletionRecord = when (habit) {
    is Habit.YesNoHabit -> Habit.YesNoHabit.CompletionRecord(
        date = date,
        numOfTimesCompleted = numOfTimesCompleted,
    )
}