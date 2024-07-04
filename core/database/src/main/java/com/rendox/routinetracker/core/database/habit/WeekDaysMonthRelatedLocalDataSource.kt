package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.model.WeekDayMonthRelated

interface WeekDaysMonthRelatedLocalDataSource {
    fun insertWeekDaysMonthRelated(
        scheduleId: Long,
        weekDaysMonthRelated: List<WeekDayMonthRelated>,
    )
    fun getWeekDayMonthRelatedDays(scheduleId: Long): List<WeekDayMonthRelated>
    fun deleteWeekDayMonthRelatedDays(scheduleId: Long)
}