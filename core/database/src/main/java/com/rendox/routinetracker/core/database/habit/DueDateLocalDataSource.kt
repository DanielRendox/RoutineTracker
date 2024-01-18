package com.rendox.routinetracker.core.database.habit

interface DueDateLocalDataSource {
    fun insertDueDates(dueDates: List<Int>, scheduleId: Long)
    fun getDueDates(scheduleId: Long): List<Int>
    fun deleteDueDates(scheduleId: Long)
}