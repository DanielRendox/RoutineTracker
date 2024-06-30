package com.rendox.routinetracker.add_edit_routine.tweak_routine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.daysOfWeek
import com.rendox.routinetracker.add_edit_routine.AddHabitDestinationTopAppBar
import com.rendox.routinetracker.add_edit_routine.navigation.AddRoutineDestination
import com.rendox.routinetracker.core.ui.components.CustomIconSetting
import com.rendox.routinetracker.core.ui.components.Setting
import com.rendox.routinetracker.core.ui.components.SingleDatePickerDialog
import com.rendox.routinetracker.core.ui.components.TonalDataInput
import com.rendox.routinetracker.core.ui.components.TonalDropdownMenu
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.feature.add_edit_routine.R
import kotlinx.datetime.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields

@Composable
fun TweakRoutinePage(
    modifier: Modifier = Modifier,
    tweakRoutinePageState: TweakRoutinePageState,
) {
    when (tweakRoutinePageState.dialogType) {
        TweakRoutinePageDialogType.StartDatePicker -> {
            SingleDatePickerDialog(
                initialDate = tweakRoutinePageState.startDate,
                dismissButtonOnClick = {
                    tweakRoutinePageState.updateDialogType(null)
                },
                confirmButtonOnClick = {
                    tweakRoutinePageState.updateStartDate(it)
                    tweakRoutinePageState.updateDialogType(null)
                }
            )
        }

        TweakRoutinePageDialogType.EndDatePicker -> {
            SingleDatePickerDialog(
                initialDate = tweakRoutinePageState.endDate ?: LocalDate.now(),
                dismissButtonOnClick = {
                    tweakRoutinePageState.updateDialogType(null)
                },
                confirmButtonOnClick = {
                    tweakRoutinePageState.updateEndDate(it)
                    tweakRoutinePageState.updateDialogType(null)
                },
                dateIsEnabled = {
                    it >= tweakRoutinePageState.startDate
                }
            )
        }

        null -> {}
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        AddHabitDestinationTopAppBar(
            modifier = Modifier.padding(bottom = 8.dp),
            destination = AddRoutineDestination.TweakRoutine,
        )

        Column {
            Text(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                text = "Habit timeframe",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            StartDateSetting(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        tweakRoutinePageState.updateDialogType(
                            dialogType = TweakRoutinePageDialogType.StartDatePicker
                        )
                    },
                startDate = tweakRoutinePageState.startDate,
            )
            val defaultWeekStartDay = WeekFields.of(LocalLocale.current).firstDayOfWeek
            if (tweakRoutinePageState.weekStartDaySettingIsEnabled) {
                if (tweakRoutinePageState.weekStartDay == null) {
                    tweakRoutinePageState.updateWeekStartDay(defaultWeekStartDay)
                }
                WeekStartDayPickerSetting(
                    modifier = Modifier.fillMaxWidth(),
                    selectedWeekStartDay = tweakRoutinePageState.weekStartDay
                        ?: defaultWeekStartDay,
                    onDayOfWeekSelected = tweakRoutinePageState::updateWeekStartDay,
                )
            }

            EndDateSetting(
                endDate = tweakRoutinePageState.endDate,
                overallNumOfDays = tweakRoutinePageState.overallNumOfDays,
                switchEndDateEnabled = tweakRoutinePageState::switchEndDateEnabled,
                overallNumOfDaysIsValid = tweakRoutinePageState.overallNumOfDaysIsValid,
                onOverallNumOfDaysChange = tweakRoutinePageState::updateOverallNumOfDays,
                showEndDatePickerDialog = {
                    tweakRoutinePageState.updateDialogType(
                        TweakRoutinePageDialogType.EndDatePicker
                    )
                }
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        )

        Column {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 8.dp)
                    .alpha(if (tweakRoutinePageState.scheduleSupportsScheduleDeviation) 1f else 0.5f),
                text = "Schedule deviation",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Setting(
                modifier = Modifier.padding(bottom = 4.dp),
                title = stringResource(id = R.string.backlog_setting_title),
                description = if (tweakRoutinePageState.scheduleSupportsScheduleDeviation) {
                    stringResource(id = R.string.backlog_setting_description)
                } else {
                    stringResource(id = R.string.setting_unavailable_description)
                },
                isOn = tweakRoutinePageState.backlogEnabled,
                onToggle = tweakRoutinePageState::updateBacklogEnabled,
                enabled = tweakRoutinePageState.scheduleSupportsScheduleDeviation,
            )
            Setting(
                modifier = Modifier.padding(vertical = 4.dp),
                title = stringResource(id = R.string.completing_ahead_setting_title),
                description = if (tweakRoutinePageState.scheduleSupportsScheduleDeviation) {
                    stringResource(id = R.string.completing_ahead_setting_description)
                } else {
                    stringResource(id = R.string.setting_unavailable_description)
                },
                isOn = tweakRoutinePageState.completingAheadEnabled,
                onToggle = tweakRoutinePageState::updateCompletingAheadEnabled,
                enabled = tweakRoutinePageState.scheduleSupportsScheduleDeviation,
            )

            tweakRoutinePageState.periodSeparationEnabled?.let { periodSeparationEnabled ->
                Setting(
                    title = stringResource(id = R.string.period_separation_setting_title),
                    description = stringResource(id = R.string.period_separation_setting_description),
                    isOn = periodSeparationEnabled,
                    onToggle = tweakRoutinePageState::updatePeriodSeparationEnabled,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        )
    }
}

@Composable
private fun StartDateSetting(
    modifier: Modifier = Modifier,
    startDate: LocalDate,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Column(modifier = modifier) {
        CustomIconSetting(
            title = stringResource(id = R.string.start_date_routine_setting_title),
            icon = {
                Icon(
                    modifier = it,
                    painter = painterResource(id = R.drawable.today_24),
                    contentDescription = null,
                )
            },
            trailingComponent = {
                TonalDataInput(
                    text = if (startDate == LocalDate.now()) {
                        stringResource(id = com.rendox.routinetracker.core.ui.R.string.today)
                    } else {
                        startDate.format(formatter)
                    }
                )
            },
        )
    }
}

@Composable
private fun EndDateSetting(
    modifier: Modifier = Modifier,
    endDate: LocalDate?,
    overallNumOfDays: String,
    switchEndDateEnabled: (Boolean) -> Unit,
    overallNumOfDaysIsValid: Boolean,
    onOverallNumOfDaysChange: (String) -> Unit,
    showEndDatePickerDialog: () -> Unit,
) {
    // for animated visibility
    var endDateBackup: LocalDate by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(endDate) {
        endDate?.let { endDateBackup = it }
    }

    CustomIconSetting(
        modifier = modifier.clickable { switchEndDateEnabled(endDate == null) },
        title = stringResource(id = R.string.end_date_routine_setting_title),
        icon = {
            Icon(
                modifier = it,
                painter = painterResource(id = R.drawable.event_24),
                contentDescription = null,
            )
        },
        trailingComponent = {
            Switch(
                checked = endDate != null,
                onCheckedChange = switchEndDateEnabled,
            )
        },
    )

    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = endDate != null,
    ) {
        EndDateDisplay(
            modifier = Modifier.padding(bottom = 8.dp),
            endDate = endDateBackup,
            overallNumOfDays = overallNumOfDays,
            onOverallNumOfDaysChange = onOverallNumOfDaysChange,
            overallNumOfDaysIsValid = overallNumOfDaysIsValid,
            showEndDatePickerDialog = showEndDatePickerDialog,
        )
    }
}

@Composable
private fun EndDateDisplay(
    modifier: Modifier = Modifier,
    endDate: LocalDate,
    overallNumOfDays: String,
    overallNumOfDaysIsValid: Boolean,
    onOverallNumOfDaysChange: (String) -> Unit,
    showEndDatePickerDialog: () -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TonalDataInput(
                modifier = Modifier.padding(end = 16.dp),
                onClick = showEndDatePickerDialog,
                disabled = false,
                text = endDate.format(formatter),
            )

            Icon(
                modifier = Modifier.padding(8.dp),
                painter = painterResource(id = R.drawable.baseline_error_24),
                contentDescription = stringResource(
                    id = R.string.error_message_num_of_days_is_not_valid
                ),
                tint = if (overallNumOfDaysIsValid) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.error
                },
            )

            TextField(
                modifier = Modifier.widthIn(max = 210.dp),
                value = overallNumOfDays,
                onValueChange = onOverallNumOfDaysChange,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                suffix = {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = stringResource(id = R.string.days_from_start_to_end_description),
                    )
                },
            )
        }
    }
}

@Composable
private fun WeekStartDayPickerSetting(
    modifier: Modifier = Modifier,
    selectedWeekStartDay: DayOfWeek,
    onDayOfWeekSelected: (DayOfWeek) -> Unit,
) {
    var weekStartDatePickerIsExpanded by remember { mutableStateOf(false) }
    CustomIconSetting(
        modifier = modifier,
        title = stringResource(id = R.string.week_start_day_setting_title),
        trailingComponent = {
            val daysOfWeek = daysOfWeek(
                firstDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek
            )
            TonalDropdownMenu(
                expanded = weekStartDatePickerIsExpanded,
                onDismissRequest = { weekStartDatePickerIsExpanded = false },
                onOptionSelected = { optionIndex ->
                    onDayOfWeekSelected(daysOfWeek[optionIndex])
                    weekStartDatePickerIsExpanded = false
                },
                selectedOptionIndex = daysOfWeek.indexOf(selectedWeekStartDay),
                onDropdownMenuClick = {
                    weekStartDatePickerIsExpanded = !weekStartDatePickerIsExpanded
                },
                options = daysOfWeek.map {
                    it.getDisplayName(TextStyle.FULL, LocalLocale.current)
                }
            )
        }
    )
}

@Preview(showSystemUi = true)
@Composable
fun TweakRoutinePagePreview() {
    Surface {
        TweakRoutinePage(
            tweakRoutinePageState = rememberTweakRoutinePageState()
        )
    }
}