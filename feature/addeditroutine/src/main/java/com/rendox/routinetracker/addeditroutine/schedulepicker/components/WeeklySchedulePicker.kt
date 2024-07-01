package com.rendox.routinetracker.addeditroutine.schedulepicker.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.daysOfWeek
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.WeeklySchedulePickerState
import com.rendox.routinetracker.addeditroutine.schedulepicker.states.rememberWeeklySchedulePickerState
import com.rendox.routinetracker.core.ui.R
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import kotlinx.datetime.DayOfWeek

@Composable
fun WeeklySchedulePicker(
    modifier: Modifier = Modifier,
    weeklySchedulePickerState: WeeklySchedulePickerState,
    selectSchedule: () -> Unit,
) {
    Column(modifier = modifier) {
        ScheduleTypeOption(
            label = stringResource(id = ScheduleTypeUi.WeeklySchedule.titleId),
            selected = weeklySchedulePickerState.selected,
            onSelected = selectSchedule,
        )

        AnimatedVisibility(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth(),
            visible = weeklySchedulePickerState.selected,
        ) {
            Column {
                val days = pluralStringResource(
                    id = R.plurals.num_of_days,
                    count = try {
                        weeklySchedulePickerState.numOfDueDays.toInt()
                    } catch (e: NumberFormatException) {
                        0
                    },
                )

                val perWeek =
                    stringResource(R.string.frequency_weekly)

                NumOfDueDaysInput(
                    numOfDueDays = weeklySchedulePickerState.numOfDueDays,
                    numOfDueDaysIsValid = weeklySchedulePickerState.numOfDueDaysIsValid,
                    isEditable = weeklySchedulePickerState.numOfDueDaysTextFieldIsEditable,
                    supportiveText = "$days $perWeek",
                    updateNumOfDueDays = weeklySchedulePickerState::updateNumOfDueDays,
                )

                val startDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek
                ChooseSpecificDaysSwitch(
                    modifier = Modifier.padding(top = 6.dp),
                    checked = weeklySchedulePickerState.chooseSpecificDays,
                    onToggle = { weeklySchedulePickerState.toggleChooseSpecificDays(startDayOfWeek) },
                )

                AnimatedVisibility(
                    visible = weeklySchedulePickerState.chooseSpecificDays,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    DayOfWeekPicker(
                        modifier = Modifier.padding(top = 4.dp),
                        selectedDays = weeklySchedulePickerState.specificDaysOfWeek,
                        daysOfWeek = daysOfWeek(startDayOfWeek),
                        toggleDayOfWeek = weeklySchedulePickerState::toggleDayOfWeek,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekPicker(
    modifier: Modifier = Modifier,
    selectedDays: List<DayOfWeek>,
    daysOfWeek: List<DayOfWeek>,
    toggleDayOfWeek: (DayOfWeek) -> Unit,
) {
    Row(modifier = modifier) {
        for (dayOfWeek in daysOfWeek) {
            DayPickerElement(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .height(40.dp)
                    .widthIn(min = 40.dp),
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, LocalLocale.current),
                selected = dayOfWeek in selectedDays,
                onToggle = { toggleDayOfWeek(dayOfWeek) },
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun WeeklySchedulePickerPreview() {
    Surface {
        Box {
            WeeklySchedulePicker(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                weeklySchedulePickerState = rememberWeeklySchedulePickerState(),
                selectSchedule = { },
            )
        }
    }
}