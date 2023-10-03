package com.rendox.routinetracker.core.database.model

import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.toDayOfWeek
import com.rendox.routinetracker.core.database.toLocalDate
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.model.WeekDayMonthRelated

internal fun constructEveryDaySchedule() = Schedule.EveryDaySchedule

internal fun ScheduleEntity.toWeeklySchedule(dueDates: List<Int>) = Schedule.WeeklySchedule(
    dueDaysOfWeek = dueDates.map { it.toDayOfWeek() },
    startDayOfWeek = startDayOfWeekInWeeklySchedule,
)

internal fun ScheduleEntity.toMonthlySchedule(
    dueDatesIndices: List<Int>,
    weekDaysMonthRelated: List<WeekDayMonthRelated>,
) = Schedule.MonthlySchedule(
    dueDatesIndices = dueDatesIndices,
    includeLastDayOfMonth = includeLastDayOfMonthInMonthlySchedule!!,
    weekDaysMonthRelated = weekDaysMonthRelated,
)

internal fun ScheduleEntity.toPeriodicCustomSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.PeriodicCustomSchedule(
    dueDatesIndices = dueDatesIndices,
    numOfDays = numOfDaysInPeriodicSchedule!!,
)

internal fun constructCustomDateSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.CustomDateSchedule(
    dueDates = dueDatesIndices.map { it.toLocalDate() },
)