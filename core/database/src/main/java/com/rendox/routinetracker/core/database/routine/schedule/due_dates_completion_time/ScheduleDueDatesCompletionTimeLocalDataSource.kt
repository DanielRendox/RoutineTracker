package com.rendox.routinetracker.core.database.routine.schedule.due_dates_completion_time

import kotlinx.datetime.LocalTime

interface ScheduleDueDatesCompletionTimeLocalDataSource {

    suspend fun updateDueDateCompletionTime(time: LocalTime, routineId: Long, dueDateNumber: Int)

    suspend fun getDueDateCompletionTime(routineId: Long, dueDateNumber: Int): LocalTime?
}