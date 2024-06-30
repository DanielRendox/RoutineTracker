package com.rendox.routinetracker.core.database.completion_time

import kotlinx.datetime.LocalTime

interface DueDateSpecificCompletionTimeLocalDataSource {
    suspend fun getDueDateSpecificCompletionTime(
        scheduleId: Long,
        dueDateNumber: Int,
    ): LocalTime?

    suspend fun updateDueDateSpecificCompletionTime(
        newTime: LocalTime,
        scheduleId: Long,
        dueDateNumber: Int,
    )
}