package com.rendox.routinetracker.core.database.routine.model

import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.database.di.toAnnualDate
import com.rendox.routinetracker.core.database.di.toDayOfWeek
import com.rendox.routinetracker.core.database.di.toLocalDate
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import kotlinx.datetime.LocalDate

internal fun ScheduleEntity.toEveryDaySchedule() = Schedule.EveryDaySchedule(
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
)

internal fun ScheduleEntity.toWeeklyScheduleByDueDaysOfWeek(
    dueDates: List<Int>
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
)

internal fun ScheduleEntity.toWeeklyScheduleByNumOfDueDays() = Schedule.WeeklyScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    numOfCompletedDaysInCurrentPeriod = numOfCompletedDaysInCurrentPeriodInByNumOfDueDaysSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toMonthlyScheduleByDueDatesIndices(
    dueDatesIndices: List<Int>,
    weekDaysMonthRelated: List<WeekDayMonthRelated>,
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
)

internal fun ScheduleEntity.toMonthlyScheduleByNumOfDueDays() = Schedule.MonthlyScheduleByNumOfDueDays(
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
)

internal fun ScheduleEntity.toPeriodicCustomSchedule() = Schedule.PeriodicCustomSchedule (
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDaysInPeriod = numOfDaysInPeriodicCustomSchedule!!,
    numOfCompletedDaysInCurrentPeriod = numOfCompletedDaysInCurrentPeriodInByNumOfDueDaysSchedule!!,
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toCustomDateSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.CustomDateSchedule(
    dueDates = dueDatesIndices.map { it.toLocalDate() },
    routineStartDate = routineStartDate,
    routineEndDate = routineEndDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    cancelDuenessIfDoneAhead = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toAnnualScheduleByDueDates(
    dueDates: List<Int>,
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
)

internal fun ScheduleEntity.toAnnualScheduleByNumOfDueDays() = Schedule.AnnualScheduleByNumOfDueDays(
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
)