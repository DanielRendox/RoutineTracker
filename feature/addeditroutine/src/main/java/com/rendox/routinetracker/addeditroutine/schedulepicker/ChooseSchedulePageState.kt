package com.rendox.routinetracker.addeditroutine.schedulepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.rendox.routinetracker.addeditroutine.schedulepicker.components.ScheduleTypeUi
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.AlternateDaysSchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.EveryDaySchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.MonthlySchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.SchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.WeeklySchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.rememberAlternateDaysSchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.rememberEveryDaySchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.rememberMonthlySchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.rememberWeeklySchedulePickerState
import com.rendox.routinetracker.core.model.Schedule

@Stable
class ChooseSchedulePageState(
    val everyDaySchedulePickerState: EveryDaySchedulePickerState,
    val weeklySchedulePickerState: WeeklySchedulePickerState,
    val monthlySchedulePickerState: MonthlySchedulePickerState,
    val alternateDaysSchedulePickerState: AlternateDaysSchedulePickerState,
) {
    val containsError: Boolean
        get() = (weeklySchedulePickerState.selected && weeklySchedulePickerState.containsError) ||
            (monthlySchedulePickerState.selected && monthlySchedulePickerState.containsError) ||
            (alternateDaysSchedulePickerState.selected && alternateDaysSchedulePickerState.containsError)

    private val schedulePickerStateList: List<SchedulePickerState> = listOf(
        everyDaySchedulePickerState,
        weeklySchedulePickerState,
        monthlySchedulePickerState,
        alternateDaysSchedulePickerState,
    )

    val selectedSchedulePickerState: SchedulePickerState
        get() = schedulePickerStateList.first { it.selected }

    fun selectSchedule(selectedScheduleType: ScheduleTypeUi) {
        for (schedulePickerState in schedulePickerStateList) {
            val isSelected = schedulePickerState.scheduleType == selectedScheduleType
            schedulePickerState.updateSelected(isSelected)
        }
    }

    fun updateSelectedSchedule(schedule: Schedule) = when (schedule) {
        is Schedule.EveryDaySchedule -> {
            selectSchedule(ScheduleTypeUi.EveryDaySchedule)
        }
        is Schedule.WeeklySchedule -> {
            selectSchedule(ScheduleTypeUi.WeeklySchedule)
            weeklySchedulePickerState.updateSelectedSchedule(schedule)
        }
        is Schedule.MonthlySchedule -> {
            selectSchedule(ScheduleTypeUi.MonthlySchedule)
            monthlySchedulePickerState.updateSelectedSchedule(schedule)
        }
        is Schedule.AlternateDaysSchedule -> {
            selectSchedule(ScheduleTypeUi.AlternateDaysSchedule)
            alternateDaysSchedulePickerState.updateSelectedSchedule(schedule)
        }
        else -> { }
    }

    fun triggerErrorsIfAny() {
        alternateDaysSchedulePickerState.triggerErrorsIfAny()
    }
}

@Composable
fun rememberChooseSchedulePageState(
    everyDaySchedulePickerState: EveryDaySchedulePickerState = rememberEveryDaySchedulePickerState(),
    weeklySchedulePickerState: WeeklySchedulePickerState = rememberWeeklySchedulePickerState(),
    monthlySchedulePickerState: MonthlySchedulePickerState = rememberMonthlySchedulePickerState(),
    alternateDaysSchedulePickerState: AlternateDaysSchedulePickerState = rememberAlternateDaysSchedulePickerState(),
) = remember(
    everyDaySchedulePickerState,
    weeklySchedulePickerState,
    monthlySchedulePickerState,
    alternateDaysSchedulePickerState,
) {
    ChooseSchedulePageState(
        everyDaySchedulePickerState,
        weeklySchedulePickerState,
        monthlySchedulePickerState,
        alternateDaysSchedulePickerState,
    )
}