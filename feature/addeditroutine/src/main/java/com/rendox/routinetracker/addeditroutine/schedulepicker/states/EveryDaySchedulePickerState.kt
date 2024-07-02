package com.rendox.routinetracker.addeditroutine.schedulepicker.states

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.rendox.routinetracker.addeditroutine.schedulepicker.components.ScheduleTypeUi

@Stable
class EveryDaySchedulePickerState(
    selected: Boolean = true,
) : SchedulePickerState(selected = selected) {

    override val scheduleType = ScheduleTypeUi.EveryDaySchedule

    companion object {
        val Saver: Saver<EveryDaySchedulePickerState, *> = listSaver(
            save = { everyDaySchedulePickerState ->
                listOf(everyDaySchedulePickerState.selected)
            },
            restore = { everyDayScheduleStateValues ->
                EveryDaySchedulePickerState(
                    selected = everyDayScheduleStateValues.first(),
                )
            },
        )
    }
}

@Composable
fun rememberEveryDaySchedulePickerState() = rememberSaveable(saver = EveryDaySchedulePickerState.Saver) {
    EveryDaySchedulePickerState()
}