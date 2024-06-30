package com.rendox.routinetracker.core.data.completion_time

import kotlinx.datetime.LocalTime

interface DueDateSpecificCompletionTimeRepository {
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