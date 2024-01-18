package com.rendox.routinetracker.add_routine.choose_schedule.schedule_pickers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states.MonthlySchedulePickerState
import com.rendox.routinetracker.core.ui.R

@Composable
fun MonthlySchedulePicker(
    modifier: Modifier = Modifier,
    monthlySchedulePickerState: MonthlySchedulePickerState,
    selectSchedule: () -> Unit,
) {
    Column(modifier = modifier) {
        ScheduleTypeOption(
            label = stringResource(id = ScheduleTypeUi.MonthlySchedule.titleId),
            selected = monthlySchedulePickerState.selected,
            onSelected = selectSchedule,
        )

        AnimatedVisibility(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth(),
            visible = monthlySchedulePickerState.selected,
        ) {
            Column {
                val days = pluralStringResource(
                    id = R.plurals.num_of_days,
                    count = try {
                        monthlySchedulePickerState.numOfDueDays.toInt()
                    } catch (e: NumberFormatException) {
                        0
                    },
                )

                val perMonth =
                    stringResource(id = R.string.frequency_monthly)

                NumOfDueDaysInput(
                    numOfDueDays = monthlySchedulePickerState.numOfDueDays,
                    numOfDueDaysIsValid = monthlySchedulePickerState.numOfDueDaysIsValid,
                    isEditable = monthlySchedulePickerState.numOfDueDaysTextFieldIsEditable,
                    supportiveText = "$days $perMonth",
                    updateNumOfDueDays = monthlySchedulePickerState::updateNumOfDueDays,
                )

                ChooseSpecificDaysSwitch(
                    modifier = Modifier.padding(top = 6.dp),
                    checked = monthlySchedulePickerState.chooseSpecificDays,
                    onToggle = monthlySchedulePickerState::toggleChooseSpecificDays,
                )

                AnimatedVisibility(
                    visible = monthlySchedulePickerState.chooseSpecificDays,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    DayOfMonthPicker(
                        selectedDays = monthlySchedulePickerState.specificDaysOfMonth,
                        lastDayOfMonthIsSelected = monthlySchedulePickerState.lastDayOfMonthSelected,
                        toggleDayOfMonth = monthlySchedulePickerState::toggleDayOfMonth,
                        toggleLastDayOfMonth = monthlySchedulePickerState::toggleLastDayOfMonth,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayOfMonthPicker(
    modifier: Modifier = Modifier,
    selectedDays: List<Int>,
    lastDayOfMonthIsSelected: Boolean,
    toggleDayOfMonth: (Int) -> Unit,
    toggleLastDayOfMonth: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val staticDaysOfMonth = remember { listOf(1..7, 8..14, 15..21, 22..28) }
        for (daysOfMonthRow in staticDaysOfMonth) {
            Row {
                for (dayOfMonth in daysOfMonthRow) {
                    DayPickerElement(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .size(40.dp),
                        text = dayOfMonth.toString(),
                        selected = dayOfMonth in selectedDays,
                        onToggle = { toggleDayOfMonth(dayOfMonth) },
                    )
                }
            }
        }
        Row {
            DayPickerElement(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .size(40.dp),
                text = "30",
                selected = 30 in selectedDays,
                onToggle = { toggleDayOfMonth(30) },
            )
            DayPickerElement(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .size(40.dp),
                text = "31",
                selected = 31 in selectedDays,
                onToggle = { toggleDayOfMonth(31) },
            )
            DayPickerElement(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .height(40.dp)
                    .wrapContentWidth()
                    .widthIn(min = 40.dp),
                text = stringResource(
                    id = com.rendox.routinetracker.feature.agenda.R.string.monthly_picker_last_day_of_month_label
                ),
                selected = lastDayOfMonthIsSelected,
                onToggle = toggleLastDayOfMonth,
            )
        }
    }
}