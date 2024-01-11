package com.rendox.routinetracker.core.database.habit.model

import com.rendox.routinetracker.core.database.di.toAnnualDate
import com.rendox.routinetracker.core.database.di.toDayOfWeek
import com.rendox.routinetracker.core.database.di.toLocalDate
import com.rendox.routinetracker.core.database.schedule.ScheduleEntity
import com.rendox.routinetracker.core.logic.time.WeekDayMonthRelated
import com.rendox.routinetracker.core.model.Schedule

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

fun ScheduleEntity.toExternalModel(
    dueDatesProvider: (Long) -> List<Int>,
    weekDaysMonthRelatedProvider: (Long) -> List<WeekDayMonthRelated>
): Schedule {
    return when (type) {
        ScheduleType.EveryDaySchedule -> toEveryDaySchedule()
        ScheduleType.WeeklyScheduleByDueDaysOfWeek ->
            toWeeklyScheduleByDueDaysOfWeek(dueDatesProvider(id))

        ScheduleType.WeeklyScheduleByNumOfDueDays ->
            toWeeklyScheduleByNumOfDueDays()

        ScheduleType.MonthlyScheduleByDueDatesIndices -> {
            val weekDaysMonthRelated = weekDaysMonthRelatedProvider(id)
            toMonthlyScheduleByDueDatesIndices(
                dueDatesProvider(id), weekDaysMonthRelated
            )
        }

        ScheduleType.MonthlyScheduleByNumOfDueDays ->
            toMonthlyScheduleByNumOfDueDays()

        ScheduleType.PeriodicCustomSchedule -> toPeriodicCustomSchedule()
        ScheduleType.CustomDateSchedule ->
            toCustomDateSchedule(dueDatesProvider(id))

        ScheduleType.AnnualScheduleByDueDates ->
            toAnnualScheduleByDueDates(dueDatesProvider(id))

        ScheduleType.AnnualScheduleByNumOfDueDays ->
            toAnnualScheduleByNumOfDueDays()
    }
}

internal fun ScheduleEntity.toEveryDaySchedule() = Schedule.EveryDaySchedule(
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
)

internal fun ScheduleEntity.toWeeklyScheduleByDueDaysOfWeek(
    dueDates: List<Int>
) = Schedule.WeeklyScheduleByDueDaysOfWeek(
    dueDaysOfWeek = dueDates.map { it.toDayOfWeek() },
    startDayOfWeek = startDayOfWeekInWeeklySchedule,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toWeeklyScheduleByNumOfDueDays() = Schedule.WeeklyScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
)

internal fun ScheduleEntity.toMonthlyScheduleByDueDatesIndices(
    dueDatesIndices: List<Int>,
    weekDaysMonthRelated: List<WeekDayMonthRelated>,
) = Schedule.MonthlyScheduleByDueDatesIndices(
    dueDatesIndices = dueDatesIndices,
    includeLastDayOfMonth = includeLastDayOfMonthInMonthlySchedule!!,
    weekDaysMonthRelated = weekDaysMonthRelated,
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toMonthlyScheduleByNumOfDueDays() = Schedule.MonthlyScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
)

internal fun ScheduleEntity.toPeriodicCustomSchedule() = Schedule.AlternateDaysSchedule (
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDaysInPeriod = numOfDaysInPeriodicCustomSchedule!!,
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
)

internal fun ScheduleEntity.toCustomDateSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.CustomDateSchedule(
    dueDates = dueDatesIndices.map { it.toLocalDate() },
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toAnnualScheduleByDueDates(
    dueDates: List<Int>,
) = Schedule.AnnualScheduleByDueDates(
    dueDates = dueDates.map { it.toAnnualDate() },
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toAnnualScheduleByNumOfDueDays() = Schedule.AnnualScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    startDate = startDate,
    endDate = endDate,
    vacationStartDate = vacationStartDate,
    vacationEndDate = vacationEndDate,
    periodSeparationEnabled = periodicSeparationEnabledInPeriodicSchedule!!,
)