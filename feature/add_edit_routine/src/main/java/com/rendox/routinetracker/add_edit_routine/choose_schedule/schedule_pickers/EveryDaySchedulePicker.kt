package com.rendox.routinetracker.add_edit_routine.choose_schedule.schedule_pickers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.add_edit_routine.choose_schedule.schedule_picker_states.EveryDaySchedulePickerState
import com.rendox.routinetracker.add_edit_routine.choose_schedule.schedule_picker_states.rememberEveryDaySchedulePickerState

@Composable
fun EveryDaySchedulePicker(
    modifier: Modifier = Modifier,
    everyDaySchedulePickerState: EveryDaySchedulePickerState,
    selectSchedule: () -> Unit,
) {
    ScheduleTypeOption(
        modifier = modifier,
        label = stringResource(id = ScheduleTypeUi.EveryDaySchedule.titleId),
        selected = everyDaySchedulePickerState.selected,
        onSelected = selectSchedule,
    )
}

@Preview(showSystemUi = true)
@Composable
private fun EveryDaySchedulePickerPreview() {
    EveryDaySchedulePicker(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        everyDaySchedulePickerState = rememberEveryDaySchedulePickerState(),
        selectSchedule = { },
    )
}