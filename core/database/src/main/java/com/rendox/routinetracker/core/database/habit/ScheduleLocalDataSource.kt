package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.model.Schedule

interface ScheduleLocalDataSource {
    fun insertSchedule(schedule: Schedule)
    fun getScheduleById(scheduleId: Long): Schedule
    fun getAllSchedules(): List<Schedule>
    fun deleteSchedule(scheduleId: Long)
}