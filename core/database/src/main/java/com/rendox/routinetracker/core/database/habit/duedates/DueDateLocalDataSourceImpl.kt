package com.rendox.routinetracker.core.database.habit.duedates

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase

internal class DueDateLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
) : DueDateLocalDataSource {
    override fun insertDueDates(
        dueDates: List<Int>,
        scheduleId: Long,
    ) {
        db.dueDateEntityQueries.transaction {
            for (dueDate in dueDates) {
                db.dueDateEntityQueries.insertDueDate(
                    scheduleId = scheduleId,
                    dueDateNumber = dueDate,
                    completionTimeHour = null,
                    completionTimeMinute = null,
                )
            }
        }
    }

    override fun getDueDates(scheduleId: Long): List<Int> =
        db.dueDateEntityQueries.getDueDates(scheduleId).executeAsList()

    override fun deleteDueDates(scheduleId: Long) {
        db.dueDateEntityQueries.deleteDueDates(scheduleId)
    }
}