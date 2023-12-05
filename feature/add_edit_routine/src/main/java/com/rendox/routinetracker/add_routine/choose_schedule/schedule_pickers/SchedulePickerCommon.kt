package com.rendox.routinetracker.add_routine.choose_schedule.schedule_pickers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.feature.agenda.R

@Composable
fun ScheduleTypeOption(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelected
        )

        val textInteractionSource = remember { MutableInteractionSource() }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = textInteractionSource,
                    indication = null,
                    onClick = onSelected,
                ),
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun NumOfDueDaysInput(
    modifier: Modifier = Modifier,
    numOfDueDays: String,
    numOfDueDaysIsValid: Boolean,
    isEditable: Boolean,
    supportiveText: String,
    updateNumOfDueDays: (String) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier
                .padding(end = 8.dp)
                .width(96.dp)
                .wrapContentHeight(),
            value = numOfDueDays,
            onValueChange = updateNumOfDueDays,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            leadingIcon = {
                if (!numOfDueDaysIsValid) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_error_24),
                        contentDescription = stringResource(
                            id = R.string.error_message_num_of_days_is_not_valid
                        ),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            },
            isError = !numOfDueDaysIsValid,
            enabled = isEditable,
        )

        Text(
            modifier = Modifier.weight(2f),
            text = supportiveText,
        )
    }
}

@Composable
fun ChooseSpecificDaysSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.weekly_picker_switch_text),
            modifier = Modifier.padding(end = 8.dp),
        )
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
        )
    }
}

@Composable
fun DayPickerElement(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle,
            ),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(30),
    ) {

        Box(contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

