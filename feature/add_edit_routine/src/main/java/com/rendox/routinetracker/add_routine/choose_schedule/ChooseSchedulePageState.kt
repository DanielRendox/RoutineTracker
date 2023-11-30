package com.rendox.routinetracker.add_routine.choose_schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
class ChooseSchedulePageState(
    val everyDaySchedulePickerState: EveryDaySchedulePickerState,
    val weeklySchedulePickerState: WeeklySchedulePickerState,
)

@Composable
fun rememberChooseSchedulePageState(
    everyDaySchedulePickerState: EveryDaySchedulePickerState = rememberEveryDaySchedulePickerState(),
    weeklySchedulePickerState: WeeklySchedulePickerState = rememberWeeklySchedulePickerState(),
) = remember(
    everyDaySchedulePickerState,
    weeklySchedulePickerState,
) {
    ChooseSchedulePageState(
        everyDaySchedulePickerState = everyDaySchedulePickerState,
        weeklySchedulePickerState = weeklySchedulePickerState,
    )
}

