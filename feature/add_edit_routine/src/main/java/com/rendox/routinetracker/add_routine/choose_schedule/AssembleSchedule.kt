package com.rendox.routinetracker.add_routine.choose_schedule

import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.AlternateDaysSchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.EveryDaySchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.MonthlySchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.SchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.WeeklySchedulePickerState
import com.rendox.routinetracker.add_routine.tweak_routine.TweakRoutinePageState
import com.rendox.routinetracker.core.model.Schedule
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn

fun SchedulePickerState.assembleSchedule(
    tweakRoutinePageState: TweakRoutinePageState? = null,
): Schedule = when (this) {
    is EveryDaySchedulePickerState -> assembleEveryDaySchedule(tweakRoutinePageState)
    is WeeklySchedulePickerState -> assembleWeeklySchedule(tweakRoutinePageState)
    is MonthlySchedulePickerState -> assembleMonthlySchedule(tweakRoutinePageState)
    is AlternateDaysSchedulePickerState -> assembleAlternateDaysSchedule(tweakRoutinePageState)
}

private fun assembleEveryDaySchedule(
    tweakRoutinePageState: TweakRoutinePageState?
): Schedule.EveryDaySchedule = Schedule.EveryDaySchedule(
    startDate = tweakRoutinePageState?.startDate?.toKotlinLocalDate()
        ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
    endDate = tweakRoutinePageState?.endDate?.toKotlinLocalDate(),
)

private fun WeeklySchedulePickerState.assembleWeeklySchedule(
    tweakRoutinePageState: TweakRoutinePageState? = null
): Schedule = if (specificDaysOfWeek.isNotEmpty() && chooseSpecificDays) {
    if (tweakRoutinePageState != null) {
        Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = specificDaysOfWeek,
            startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
            endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
            backlogEnabled = tweakRoutinePageState.backlogEnabled,
            completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled,
            startDayOfWeek = tweakRoutinePageState.weekStartDay!!,
        )
    } else {
        Schedule.WeeklyScheduleByDueDaysOfWeek(
            dueDaysOfWeek = specificDaysOfWeek,
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        )
    }
} else {
    if (tweakRoutinePageState != null) {
        Schedule.WeeklyScheduleByNumOfDueDays(
            numOfDueDays = numOfDueDays.toInt(),
            startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
            endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
            startDayOfWeek = tweakRoutinePageState.weekStartDay!!,
        )
    } else {
        Schedule.WeeklyScheduleByNumOfDueDays(
            numOfDueDays = numOfDueDays.toInt(),
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        )
    }
}

private fun MonthlySchedulePickerState.assembleMonthlySchedule(
    tweakRoutinePageState: TweakRoutinePageState?
): Schedule = if (specificDaysOfMonth.isNotEmpty() && chooseSpecificDays) {
    if (tweakRoutinePageState != null) {
        Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = specificDaysOfMonth,
            includeLastDayOfMonth = lastDayOfMonthSelected,
            startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
            endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
            backlogEnabled = tweakRoutinePageState.backlogEnabled,
            completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled,
            startFromHabitStart = false,
        )
    } else {
        Schedule.MonthlyScheduleByDueDatesIndices(
            dueDatesIndices = specificDaysOfMonth,
            includeLastDayOfMonth = lastDayOfMonthSelected,
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            startFromHabitStart = false,
        )
    }
} else {
    if (tweakRoutinePageState != null) {
        Schedule.MonthlyScheduleByNumOfDueDays(
            numOfDueDays = numOfDueDays.toInt(),
            startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
            endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
            startFromHabitStart = false,
        )
    } else {
        Schedule.MonthlyScheduleByNumOfDueDays(
            numOfDueDays = numOfDueDays.toInt(),
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            startFromHabitStart = false,
        )
    }
}

private fun AlternateDaysSchedulePickerState.assembleAlternateDaysSchedule(
    tweakRoutinePageState: TweakRoutinePageState?,
): Schedule {
    val numOfActivityDays = numOfActivityDays.toInt()
    val numOfRestDays = numOfRestDays.toInt()

    return if (tweakRoutinePageState != null) {
        Schedule.AlternateDaysSchedule(
            numOfDueDays = numOfActivityDays,
            numOfDaysInPeriod = numOfActivityDays + numOfRestDays,
            startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
            endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
            backlogEnabled = tweakRoutinePageState.backlogEnabled,
            completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled,
        )
    } else {
        Schedule.AlternateDaysSchedule(
            numOfDueDays = numOfActivityDays,
            numOfDaysInPeriod = numOfActivityDays + numOfRestDays,
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        )
    }
}