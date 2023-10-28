package com.rendox.routinetracker.core.database.routine.model

import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.toAnnualDate
import com.rendox.routinetracker.core.database.toDayOfWeek
import com.rendox.routinetracker.core.database.toLocalDate
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated

internal fun ScheduleEntity.toEveryDaySchedule() = Schedule.EveryDaySchedule(
    routineStartDate = startDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
)

internal fun ScheduleEntity.toWeeklySchedule(dueDates: List<Int>) = Schedule.WeeklyScheduleByDueDaysOfWeek(
    dueDaysOfWeek = dueDates.map { it.toDayOfWeek() },
    startDayOfWeek = startDayOfWeekInWeeklySchedule,
    routineStartDate = startDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
)

internal fun ScheduleEntity.toMonthlySchedule(
    dueDatesIndices: List<Int>,
    weekDaysMonthRelated: List<WeekDayMonthRelated>,
) = Schedule.MonthlyScheduleByDueDatesIndices(
    dueDatesIndices = dueDatesIndices,
    includeLastDayOfMonth = includeLastDayOfMonthInMonthlySchedule!!,
    weekDaysMonthRelated = weekDaysMonthRelated,
    startFromRoutineStart = startFromRoutineStartInMonthlySchedule!!,
    routineStartDate = startDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
)

internal fun ScheduleEntity.toPeriodicCustomSchedule(
    dueDatesIndices: List<Int>,
): Schedule.PeriodicCustomSchedule {
    TODO()
}

internal fun ScheduleEntity.toCustomDateSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.CustomDateSchedule(
    dueDates = dueDatesIndices.map { it.toLocalDate() },
    routineStartDate = startDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
)

internal fun ScheduleEntity.toAnnualSchedule(
    dueDates: List<Int>,
) = Schedule.AnnualScheduleByDueDates(
    dueDates = dueDates.map { it.toAnnualDate() },
    routineStartDate = startDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    startFromRoutineStart = startDayOfYearInAnnualSchedule != null, // TODO
)