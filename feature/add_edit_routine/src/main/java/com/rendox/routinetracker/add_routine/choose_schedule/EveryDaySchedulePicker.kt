package com.rendox.routinetracker.add_routine.choose_schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun EveryDaySchedulePicker(
    modifier: Modifier = Modifier,
    everyDaySchedulePickerState: EveryDaySchedulePickerState,
) {
    ScheduleTypeOption(
        modifier = modifier,
        label = stringResource(id = ScheduleTypeUi.EveryDaySchedule.titleId),
        selected = everyDaySchedulePickerState.selected,
        onSelected = everyDaySchedulePickerState::selectOption,
    )
}

@Stable
class EveryDaySchedulePickerState(
    selected: Boolean = true,
) {
    var selected by mutableStateOf(selected)
        private set

    fun selectOption() {
        selected = true
    }

    companion object {
        val Saver: Saver<EveryDaySchedulePickerState, *> = listSaver(
            save = { everyDaySchedulePickerState ->
                listOf(everyDaySchedulePickerState.selected)
            },
            restore = { everyDayScheduleStateValues ->
                EveryDaySchedulePickerState(
                    selected = everyDayScheduleStateValues.first()
                )
            },
        )
    }
}

@Composable
fun rememberEveryDaySchedulePickerState() =
    rememberSaveable(saver = EveryDaySchedulePickerState.Saver) {
        EveryDaySchedulePickerState()
    }