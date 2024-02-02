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
    AlternateDaysSchedule,
    CustomDateSchedule,
    AnnualScheduleByDueDates,
    AnnualScheduleByNumOfDueDays,
}

internal fun ScheduleEntity.toExternalModel(
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

        ScheduleType.AlternateDaysSchedule -> toAlternateDaySchedule()
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
)

internal fun ScheduleEntity.toWeeklyScheduleByDueDaysOfWeek(
    dueDates: List<Int>
) = Schedule.WeeklyScheduleByDueDaysOfWeek(
    dueDaysOfWeek = dueDates.map { it.toDayOfWeek() },
    startDayOfWeek = startDayOfWeekInWeeklySchedule,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toWeeklyScheduleByNumOfDueDays() = Schedule.WeeklyScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    startDate = startDate,
    endDate = endDate,
    startDayOfWeek = startDayOfWeekInWeeklySchedule,
)

internal fun ScheduleEntity.toMonthlyScheduleByDueDatesIndices(
    dueDatesIndices: List<Int>,
    weekDaysMonthRelated: List<WeekDayMonthRelated>,
) = Schedule.MonthlyScheduleByDueDatesIndices(
    dueDatesIndices = dueDatesIndices,
    includeLastDayOfMonth = includeLastDayOfMonthInMonthlySchedule!!,
    weekDaysMonthRelated = weekDaysMonthRelated,
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toMonthlyScheduleByNumOfDueDays() = Schedule.MonthlyScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    startDate = startDate,
    endDate = endDate,
)

internal fun ScheduleEntity.toAlternateDaySchedule() = Schedule.AlternateDaysSchedule (
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDaysInPeriod = numOfDaysInAlternateDaysSchedule!!,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toCustomDateSchedule(
    dueDatesIndices: List<Int>,
) = Schedule.CustomDateSchedule(
    dueDates = dueDatesIndices.map { it.toLocalDate() },
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toAnnualScheduleByDueDates(
    dueDates: List<Int>,
) = Schedule.AnnualScheduleByDueDates(
    dueDates = dueDates.map { it.toAnnualDate() },
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    startDate = startDate,
    endDate = endDate,
    backlogEnabled = backlogEnabled,
    completingAheadEnabled = cancelDuenessIfDoneAhead,
)

internal fun ScheduleEntity.toAnnualScheduleByNumOfDueDays() = Schedule.AnnualScheduleByNumOfDueDays(
    numOfDueDays = numOfDueDaysInByNumOfDueDaysSchedule!!,
    numOfDueDaysInFirstPeriod = numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule,
    startFromHabitStart = startFromHabitStartInMonthlyAndAnnualSchedule!!,
    startDate = startDate,
    endDate = endDate,
)