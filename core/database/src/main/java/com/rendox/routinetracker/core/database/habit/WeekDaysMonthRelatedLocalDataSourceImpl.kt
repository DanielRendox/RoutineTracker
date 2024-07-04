package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.di.toDayOfWeek
import com.rendox.routinetracker.core.database.di.toInt
import com.rendox.routinetracker.core.model.WeekDayMonthRelated

internal class WeekDaysMonthRelatedLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
) : WeekDaysMonthRelatedLocalDataSource {
    override fun insertWeekDaysMonthRelated(
        scheduleId: Long,
        weekDaysMonthRelated: List<WeekDayMonthRelated>,
    ) = db.weekDayMonthRelatedEntityQueries.transaction {
        for (weekDayMonthRelated in weekDaysMonthRelated) {
            db.weekDayMonthRelatedEntityQueries.insertWeekDayMonthRelatedEntry(
                id = null,
                scheduleId = scheduleId,
                weekDayIndex = weekDayMonthRelated.dayOfWeek.toInt(),
                weekDayNumberMonthRelated = weekDayMonthRelated.weekDayNumberMonthRelated,
            )
        }
    }

    override fun getWeekDayMonthRelatedDays(scheduleId: Long): List<WeekDayMonthRelated> =
        db.weekDayMonthRelatedEntityQueries
            .getWeekDayMonthRelatedDays(scheduleId)
            .executeAsList()
            .map { entity ->
                WeekDayMonthRelated(
                    dayOfWeek = entity.weekDayIndex.toDayOfWeek(),
                    weekDayNumberMonthRelated = entity.weekDayNumberMonthRelated,
                )
            }

    override fun deleteWeekDayMonthRelatedDays(scheduleId: Long) {
        db.weekDayMonthRelatedEntityQueries.deleteWeekDayMonthRelatedDays(scheduleId)
    }
}