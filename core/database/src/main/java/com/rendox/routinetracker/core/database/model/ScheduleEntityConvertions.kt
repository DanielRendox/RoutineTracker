package com.rendox.routinetracker.core.database.model

import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.toAnnualDate
import com.rendox.routinetracker.core.database.toDayOfWeek
import com.rendox.routinetracker.core.database.toLocalDate
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated

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
    startFromRoutineStart = startFromRoutineStartInMonthlySchedule!!,
)

internal fun ScheduleEntity.toPeriodicCustomSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.PeriodicCustomSchedule(
    dueDatesIndices = dueDatesIndices,
    numOfDaysInPeriod = numOfDaysInPeriodicSchedule!!,
)

internal fun ScheduleEntity.toAnnualSchedule(
    dueDates: List<Int>,
) = Schedule.AnnualSchedule(
    dueDates = dueDates.map { it.toAnnualDate() },
    startDayOfYear = startDayOfYearInAnnualSchedule,
)

internal fun constructCustomDateSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.CustomDateSchedule(
    dueDates = dueDatesIndices.map { it.toLocalDate() },
)