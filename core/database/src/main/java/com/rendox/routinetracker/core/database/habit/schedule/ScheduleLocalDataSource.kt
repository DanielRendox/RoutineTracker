package com.rendox.routinetracker.core.database.habit.schedule

import com.rendox.routinetracker.core.model.Schedule

interface ScheduleLocalDataSource {
    fun insertSchedule(schedule: Schedule)
    fun getScheduleById(habitId: Long): Schedule
    fun deleteSchedule(scheduleId: Long)
}