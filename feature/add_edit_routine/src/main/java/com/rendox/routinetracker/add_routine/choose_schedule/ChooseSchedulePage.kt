package com.rendox.routinetracker.add_routine.choose_schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChooseSchedulePage(
    modifier: Modifier = Modifier,
    chooseSchedulePageState: ChooseSchedulePageState,
) {

    Column(modifier = modifier) {
        EveryDaySchedulePicker(
            everyDaySchedulePickerState = chooseSchedulePageState.everyDaySchedulePickerState
        )
        WeeklySchedulePicker(
            weeklySchedulePickerState = chooseSchedulePageState.weeklySchedulePickerState
        )
    }
}

@Composable
fun ScheduleTypeOption(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Row(modifier = modifier) {
        RadioButton(selected = selected, onClick = onSelected)
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}