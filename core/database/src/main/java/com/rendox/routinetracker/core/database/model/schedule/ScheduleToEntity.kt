package com.rendox.routinetracker.core.database.model.schedule

import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.model.Schedule

internal fun Schedule.EveryDaySchedule.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.EveryDaySchedule,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = null,
    startFromHabitStartInMonthlyAndAnnualSchedule = null,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = null,
    numOfDueDaysInByNumOfDueDaysSchedule = null,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
    numOfDaysInAlternateDaysSchedule = null,
)

internal fun Schedule.WeeklyScheduleByDueDaysOfWeek.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.WeeklyScheduleByDueDaysOfWeek,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = startDayOfWeek,
    startFromHabitStartInMonthlyAndAnnualSchedule = null,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = periodSeparationEnabled,
    numOfDueDaysInByNumOfDueDaysSchedule = null,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
    numOfDaysInAlternateDaysSchedule = null,
)

internal fun Schedule.WeeklyScheduleByNumOfDueDays.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.WeeklyScheduleByNumOfDueDays,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = startDayOfWeek,
    startFromHabitStartInMonthlyAndAnnualSchedule = null,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = periodSeparationEnabled,
    numOfDueDaysInByNumOfDueDaysSchedule = numOfDueDays,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = numOfDueDaysInFirstPeriod,
    numOfDaysInAlternateDaysSchedule = null,
)

internal fun Schedule.MonthlyScheduleByDueDatesIndices.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.MonthlyScheduleByDueDatesIndices,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = null,
    startFromHabitStartInMonthlyAndAnnualSchedule = startFromHabitStart,
    includeLastDayOfMonthInMonthlySchedule = includeLastDayOfMonth,
    periodicSeparationEnabledInPeriodicSchedule = periodSeparationEnabled,
    numOfDueDaysInByNumOfDueDaysSchedule = null,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
    numOfDaysInAlternateDaysSchedule = null
)

internal fun Schedule.MonthlyScheduleByNumOfDueDays.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.MonthlyScheduleByNumOfDueDays,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = null,
    startFromHabitStartInMonthlyAndAnnualSchedule = startFromHabitStart,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = periodSeparationEnabled,
    numOfDueDaysInByNumOfDueDaysSchedule = numOfDueDays,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = numOfDueDaysInFirstPeriod,
    numOfDaysInAlternateDaysSchedule = null,
)

internal fun Schedule.AlternateDaysSchedule.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.AlternateDaysSchedule,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = null,
    startFromHabitStartInMonthlyAndAnnualSchedule = null,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = periodSeparationEnabled,
    numOfDueDaysInByNumOfDueDaysSchedule = numOfDueDays,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
    numOfDaysInAlternateDaysSchedule = numOfDaysInPeriod,
)

internal fun Schedule.CustomDateSchedule.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.CustomDateSchedule,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = null,
    startFromHabitStartInMonthlyAndAnnualSchedule = null,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = null,
    numOfDueDaysInByNumOfDueDaysSchedule = null,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
    numOfDaysInAlternateDaysSchedule = null,
)

internal fun Schedule.AnnualScheduleByDueDates.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.AnnualScheduleByDueDates,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = null,
    startFromHabitStartInMonthlyAndAnnualSchedule = startFromHabitStart,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = periodSeparationEnabled,
    numOfDueDaysInByNumOfDueDaysSchedule = null,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = null,
    numOfDaysInAlternateDaysSchedule = null,
)

internal fun Schedule.AnnualScheduleByNumOfDueDays.toScheduleEntity() = ScheduleEntity(
    id = -1,
    type = ScheduleType.AnnualScheduleByNumOfDueDays,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = completingAheadEnabled,
    startDayOfWeekInWeeklySchedule = null,
    startFromHabitStartInMonthlyAndAnnualSchedule = startFromHabitStart,
    includeLastDayOfMonthInMonthlySchedule = null,
    periodicSeparationEnabledInPeriodicSchedule = periodSeparationEnabled,
    numOfDueDaysInByNumOfDueDaysSchedule = numOfDueDays,
    numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = numOfDueDaysInFirstPeriod,
    numOfDaysInAlternateDaysSchedule = null,
)