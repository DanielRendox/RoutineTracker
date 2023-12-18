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
    tweakRoutinePageState: TweakRoutinePageState? = null
): Schedule = when (this) {
    is EveryDaySchedulePickerState -> createEveryDaySchedule(tweakRoutinePageState)
    is WeeklySchedulePickerState -> convertToScheduleModel(tweakRoutinePageState)
    is MonthlySchedulePickerState -> convertToScheduleModel(tweakRoutinePageState)
    is AlternateDaysSchedulePickerState -> convertToScheduleModel(tweakRoutinePageState)
}

private fun createEveryDaySchedule(
    tweakRoutinePageState: TweakRoutinePageState?
): Schedule.EveryDaySchedule = Schedule.EveryDaySchedule(
    startDate = tweakRoutinePageState?.startDate?.toKotlinLocalDate()
        ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
)

private fun WeeklySchedulePickerState.convertToScheduleModel(
    tweakRoutinePageState: TweakRoutinePageState? = null
): Schedule {
    if (numOfDueDays.toInt() == 7) {
        return createEveryDaySchedule(tweakRoutinePageState)
    }

    return if (specificDaysOfWeek.isNotEmpty()) {
        if (tweakRoutinePageState != null) {
            Schedule.WeeklyScheduleByDueDaysOfWeek(
                dueDaysOfWeek = specificDaysOfWeek,
                startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
                endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
                backlogEnabled = tweakRoutinePageState.backlogEnabled!!,
                completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled!!,
                periodSeparationEnabled = tweakRoutinePageState.periodSeparationEnabled!!,
            )
        } else {
            Schedule.WeeklyScheduleByDueDaysOfWeek(
                dueDaysOfWeek = specificDaysOfWeek,
                startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            )
        }
    } else {
        // TODO allow the user to choose whether to start 1st week from start date or not
        if (tweakRoutinePageState != null) {
            Schedule.WeeklyScheduleByNumOfDueDays(
                numOfDueDays = numOfDueDays.toInt(),
                startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
                endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
                backlogEnabled = tweakRoutinePageState.backlogEnabled!!,
                completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled!!,
            )
        } else {
            Schedule.WeeklyScheduleByNumOfDueDays(
                numOfDueDays = numOfDueDays.toInt(),
                startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            )
        }
    }
}

private fun MonthlySchedulePickerState.convertToScheduleModel(
    tweakRoutinePageState: TweakRoutinePageState?
): Schedule {
    if (numOfDueDays.toInt() == 31) {
        return createEveryDaySchedule(tweakRoutinePageState)
    }

    return if (specificDaysOfMonth.isNotEmpty()) {
        if (tweakRoutinePageState != null) {
            Schedule.MonthlyScheduleByDueDatesIndices(
                dueDatesIndices = specificDaysOfMonth,
                includeLastDayOfMonth = lastDayOfMonthSelected,
                weekDaysMonthRelated = emptyList(), // TODO add support for weekDaysMonthRelated,
                startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
                endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
                periodSeparationEnabled = tweakRoutinePageState.periodSeparationEnabled!!,
                backlogEnabled = tweakRoutinePageState.backlogEnabled!!,
                completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled!!,
            )
        } else {
            Schedule.MonthlyScheduleByDueDatesIndices(
                dueDatesIndices = specificDaysOfMonth,
                includeLastDayOfMonth = lastDayOfMonthSelected,
                weekDaysMonthRelated = emptyList(), // TODO add support for weekDaysMonthRelated,
                startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            )
        }
    } else {
        if (tweakRoutinePageState != null) {
            Schedule.MonthlyScheduleByNumOfDueDays(
                numOfDueDays = numOfDueDays.toInt(),
                startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
                endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
                backlogEnabled = tweakRoutinePageState.backlogEnabled!!,
                completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled!!,
            )
        } else {
            Schedule.MonthlyScheduleByNumOfDueDays(
                numOfDueDays = numOfDueDays.toInt(),
                startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            )
        }
    }
}

private fun AlternateDaysSchedulePickerState.convertToScheduleModel(
    tweakRoutinePageState: TweakRoutinePageState?
): Schedule {
    val numOfActivityDays = numOfActivityDays.toInt()
    val numOfRestDays = numOfRestDays.toInt()

    if (numOfActivityDays + numOfRestDays == 7) {
        return Schedule.WeeklyScheduleByNumOfDueDays(
            numOfDueDays = numOfActivityDays,
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        )
    }

    return if (tweakRoutinePageState != null) {
        Schedule.PeriodicCustomSchedule(
            numOfDueDays = numOfActivityDays,
            numOfDaysInPeriod = numOfActivityDays + numOfRestDays,
            startDate = tweakRoutinePageState.startDate.toKotlinLocalDate(),
            endDate = tweakRoutinePageState.endDate?.toKotlinLocalDate(),
            backlogEnabled = tweakRoutinePageState.backlogEnabled!!,
            completingAheadEnabled = tweakRoutinePageState.completingAheadEnabled!!,
        )
    } else {
        Schedule.PeriodicCustomSchedule(
            numOfDueDays = numOfActivityDays,
            numOfDaysInPeriod = numOfActivityDays + numOfRestDays,
            startDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        )
    }
}