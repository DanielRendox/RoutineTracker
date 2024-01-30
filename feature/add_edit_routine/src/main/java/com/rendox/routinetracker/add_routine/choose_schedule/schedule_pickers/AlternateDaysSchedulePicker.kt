package com.rendox.routinetracker.add_routine.choose_schedule.schedule_pickers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.AlternateDaysSchedulePickerState
import com.rendox.routinetracker.feature.agenda.R

@Composable
fun AlternateDaysSchedulePicker(
    modifier: Modifier = Modifier,
    alternateDaysSchedulePickerState: AlternateDaysSchedulePickerState,
    selectSchedule: () -> Unit,
) {
    Column(modifier = modifier) {
        ScheduleTypeOption(
            label = stringResource(id = ScheduleTypeUi.AlternateDaysSchedule.titleId),
            selected = alternateDaysSchedulePickerState.selected,
            onSelected = selectSchedule,
        )

        AnimatedVisibility(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterHorizontally),
            visible = alternateDaysSchedulePickerState.selected,
        ) {
            AlternateDaysInput(
                numOfActivityDays = alternateDaysSchedulePickerState.numOfActivityDays,
                numOfRestDays = alternateDaysSchedulePickerState.numOfRestDays,
                numOfActivityDaysIsValid = alternateDaysSchedulePickerState.numOfActivityDaysIsValid,
                numOfRestDaysIsValid = alternateDaysSchedulePickerState.numOfRestDaysIsValid,
                updateNumOfActivityDays = alternateDaysSchedulePickerState::updateNumOfActivityDays,
                updateNumOfRestDays = alternateDaysSchedulePickerState::updateNumOfRestDays,
            )
        }
    }
}

@Composable
private fun AlternateDaysInput(
    modifier: Modifier = Modifier,
    numOfActivityDays: String,
    numOfRestDays: String,
    numOfActivityDaysIsValid: Boolean,
    numOfRestDaysIsValid: Boolean,
    updateNumOfActivityDays: (String) -> Unit,
    updateNumOfRestDays: (String) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(end = 16.dp),
            painter = painterResource(id = R.drawable.baseline_error_24),
            contentDescription = stringResource(
                id = R.string.error_message_num_of_days_is_not_valid
            ),
            tint = if (!numOfActivityDaysIsValid || !numOfRestDaysIsValid) {
                MaterialTheme.colorScheme.error
            } else {
                Color.Transparent
            },
        )

        TextField(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
            value = numOfActivityDays,
            onValueChange = updateNumOfActivityDays,
            placeholder = {
                Text(
                    text = stringResource(
                        id = R.string.alternate_days_picker_activity_text_field_placeholder
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            isError = !numOfActivityDaysIsValid,
        )

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "/",
        )

        TextField(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
            value = numOfRestDays,
            onValueChange = updateNumOfRestDays,
            placeholder = {
                Text(
                    text = stringResource(
                        id = R.string.alternate_days_picker_rest_text_field_placeholder
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            isError = !numOfRestDaysIsValid,
        )
    }
}