package com.rendox.routinetracker.core.database.routine.model

import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.toAnnualDate
import com.rendox.routinetracker.core.database.toDayOfWeek
import com.rendox.routinetracker.core.database.toLocalDate
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.datetime.LocalDate

internal fun ScheduleEntity.toEveryDaySchedule(
    lastDateInHistory: LocalDate?
) = Schedule.EveryDaySchedule(
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toWeeklyScheduleByDueDaysOfWeek(
    dueDates: List<Int>, lastDateInHistory: LocalDate?
) = Schedule.WeeklyScheduleByDueDaysOfWeek(
    dueDaysOfWeek = dueDates.map { it.toDayOfWeek() },
    startDayOfWeek = startDayOfWeekInWeeklySchedule,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toWeeklyScheduleByNumOfDueDays(
    lastDateInHistory: LocalDate?
) = Schedule.WeeklyScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    numOfCompletedDaysInCurrentPeriod = numOfCompletedDaysInCurrentPeriodInByNumOfDueDaysSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toMonthlyScheduleByDueDatesIndices(
    dueDatesIndices: List<Int>,
    weekDaysMonthRelated: List<WeekDayMonthRelated>,
    lastDateInHistory: LocalDate?,
) = Schedule.MonthlyScheduleByDueDatesIndices(
    dueDatesIndices = dueDatesIndices,
    includeLastDayOfMonth = includeLastDayOfMonthInMonthlySchedule!!,
    weekDaysMonthRelated = weekDaysMonthRelated,
    startFromRoutineStart = startFromRoutineStartInMonthlyAndAnnualSchedule!!,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toMonthlyScheduleByNumOfDueDays(
    lastDateInHistory: LocalDate?
) = Schedule.MonthlyScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    numOfCompletedDaysInCurrentPeriod = numOfCompletedDaysInCurrentPeriodInByNumOfDueDaysSchedule!!,
    startFromRoutineStart = startFromRoutineStartInMonthlyAndAnnualSchedule!!,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toPeriodicCustomSchedule(
    lastDateInHistory: LocalDate?,
) = Schedule.PeriodicCustomSchedule (
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDaysInPeriod = numOfDaysInPeriodicCustomSchedule!!,
    numOfCompletedDaysInCurrentPeriod = numOfCompletedDaysInCurrentPeriodInByNumOfDueDaysSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toCustomDateSchedule(
    dueDatesIndices: List<Int>,
    lastDateInHistory: LocalDate?,
) = Schedule.CustomDateSchedule(
    dueDates = dueDatesIndices.map { it.toLocalDate() },
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toAnnualScheduleByDueDates(
    dueDates: List<Int>,
    lastDateInHistory: LocalDate?,
) = Schedule.AnnualScheduleByDueDates(
    dueDates = dueDates.map { it.toAnnualDate() },
    startFromRoutineStart = startFromRoutineStartInMonthlyAndAnnualSchedule!!,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)

internal fun ScheduleEntity.toAnnualScheduleByNumOfDueDays(
    lastDateInHistory: LocalDate?,
) = Schedule.AnnualScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    numOfCompletedDaysInCurrentPeriod = numOfCompletedDaysInCurrentPeriodInByNumOfDueDaysSchedule!!,
    startFromRoutineStart = startFromRoutineStartInMonthlyAndAnnualSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
    lastDateInHistory = lastDateInHistory,
)