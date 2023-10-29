package com.rendox.routinetracker.core.database.routine.model

enum class ScheduleType {
    EveryDaySchedule,
    WeeklyScheduleByDueDaysOfWeek,
    WeeklyScheduleByNumOfDueDays,
    MonthlyScheduleByDueDatesIndices,
    MonthlyScheduleByNumOfDueDays,
    PeriodicCustomSchedule,
    CustomDateSchedule,
    AnnualScheduleByDueDates,
    AnnualScheduleByNumOfDueDays,
}