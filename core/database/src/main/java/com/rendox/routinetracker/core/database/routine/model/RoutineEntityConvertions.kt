package com.rendox.routinetracker.core.database.routine.model

import com.rendox.routinetracker.core.database.routine.RoutineEntity
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.LocalTime

enum class RoutineType {
    YesNoRoutine,
//    MeasurableRoutine,
//    TasksRoutine,
}

fun RoutineEntity.toExternalModel(schedule: Schedule) = when (this.type) {
    RoutineType.YesNoRoutine -> this.toYesNoRoutine(
        schedule = schedule,
    )
}

internal fun RoutineEntity.toYesNoRoutine(
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