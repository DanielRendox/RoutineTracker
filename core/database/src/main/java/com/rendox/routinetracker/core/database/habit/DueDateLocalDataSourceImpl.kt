package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase

internal class DueDateLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
) : DueDateLocalDataSource {
    override fun insertDueDates(dueDates: List<Int>, scheduleId: Long) {
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

    override fun getDueDates(scheduleId: Long): List<Int> {
        return db.dueDateEntityQueries.getDueDates(scheduleId).executeAsList()
    }
}