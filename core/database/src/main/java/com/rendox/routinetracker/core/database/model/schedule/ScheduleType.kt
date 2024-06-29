package com.rendox.routinetracker.core.database.model.schedule

enum class ScheduleType {
    EveryDaySchedule,
    WeeklyScheduleByDueDaysOfWeek,
    WeeklyScheduleByNumOfDueDays,
    MonthlyScheduleByDueDatesIndices,
    MonthlyScheduleByNumOfDueDays,
    AlternateDaysSchedule,
    CustomDateSchedule,
    AnnualScheduleByDueDates,
    AnnualScheduleByNumOfDueDays,
}