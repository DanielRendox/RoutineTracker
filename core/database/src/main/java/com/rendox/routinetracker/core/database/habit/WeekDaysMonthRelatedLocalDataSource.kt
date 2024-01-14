package com.rendox.routinetracker.core.database.habit

import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated

internal interface WeekDaysMonthRelatedLocalDataSource {
    fun insertWeekDaysMonthRelated(scheduleId: Long, weekDaysMonthRelated: List<WeekDayMonthRelated>)
    fun getWeekDayMonthRelatedDays(scheduleId: Long): List<WeekDayMonthRelated>
}