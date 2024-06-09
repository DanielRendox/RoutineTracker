package com.rendox.routinetracker.core.database.habit.model

import com.rendox.routinetracker.core.database.habit.HabitEntity
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalTime

enum class HabitType {
    YesNoHabit,
//    MeasurableRoutine,
//    TasksRoutine,
}

fun HabitEntity.toExternalModel(schedule: Schedule) = when (this.type) {
    HabitType.YesNoHabit -> this.toYesNoRoutine(
        schedule = schedule,
    )
}

internal fun HabitEntity.toYesNoRoutine(
    schedule: Schedule,
): Habit {
    val defaultCompletionTime =
        if (defaultCompletionTimeHour != null && defaultCompletionTimeMinute != null) {
            LocalTime(hour = defaultCompletionTimeHour, minute = defaultCompletionTimeMinute)
        } else null

    return Habit.YesNoHabit(
        id = id,
        name = name,
        description = description,
        sessionDurationMinutes = sessionDurationMinutes,
        progress = progress,
        schedule = schedule,
        defaultCompletionTime = defaultCompletionTime,
    )
}