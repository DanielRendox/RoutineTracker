package com.rendox.routinetracker.add_routine.choose_schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_pickers.ScheduleTypeUi
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.EveryDaySchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.SchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.AlternateDaysSchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.rememberAlternateDaysSchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.WeeklySchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.rememberEveryDaySchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.MonthlySchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.rememberMonthlySchedulePickerState
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.rememberWeeklySchedulePickerState
import com.rendox.routinetracker.core.model.Schedule

@Stable
class ChooseSchedulePageState(
    val everyDaySchedulePickerState: EveryDaySchedulePickerState,
    val weeklySchedulePickerState: WeeklySchedulePickerState,
    val monthlySchedulePickerState: MonthlySchedulePickerState,
    val alternateDaysSchedulePickerState: AlternateDaysSchedulePickerState,
) {
    val containsError: Boolean
        get() = (weeklySchedulePickerState.selected && weeklySchedulePickerState.containsError)
                || (monthlySchedulePickerState.selected && monthlySchedulePickerState.containsError)
                || (alternateDaysSchedulePickerState.selected && alternateDaysSchedulePickerState.containsError)

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

