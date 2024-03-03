package com.rendox.routinetracker.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.ui.R
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

@Composable
fun SingleDatePickerDialog(
    today: LocalDate = LocalDate.now(),
    initialDate: LocalDate = today,
    dismissButtonOnClick: () -> Unit,
    confirmButtonOnClick: (LocalDate) -> Unit,
    dateIsEnabled: (LocalDate) -> Boolean = { true },
) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    val isInLandscapeMode =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Dialog(
        onDismissRequest = { dismissButtonOnClick() },
        properties = DialogProperties(
            usePlatformDefaultWidth = !isInLandscapeMode
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            if (isInLandscapeMode) {
                DatePickerCalendarContentLandscape(
                    selectedDate = selectedDate,
                    dateIsEnabled = dateIsEnabled,
                    onDateClicked = { selectedDate = it },
                    onDismissButtonClicked = { dismissButtonOnClick() },
                    onConfirmButtonClicked = { confirmButtonOnClick(selectedDate) },
                )
            } else {
                DatePickerCalendarContentPortrait(
                    selectedDate = selectedDate,
                    dateIsEnabled = dateIsEnabled,
                    onDateClicked = { selectedDate = it },
                    onDismissButtonClicked = { dismissButtonOnClick() },
                    onConfirmButtonClicked = { confirmButtonOnClick(selectedDate) },
                )
            }
        }
    }
}

@Composable
private fun DatePickerTitles(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
) {
    val selectDateTextBottomPadding = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> 16.dp
        else -> 36.dp
    }
    val textStartPadding = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> 12.dp
        else -> 24.dp
    }
    val dateTitleStyle = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.headlineMedium
    }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(
                start = textStartPadding,
                top = 16.dp,
                bottom = selectDateTextBottomPadding
            ),
            text = stringResource(id = R.string.date_picker_supporting_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            modifier = Modifier.padding(start = textStartPadding, bottom = 8.dp),
            text = selectedDate.format(DateTimeFormatter.ofPattern("E, MMM d")),
            style = dateTitleStyle,
        )
    }
}

@Composable
private fun DatePickerCalendarContentPortrait(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    dateIsEnabled: (LocalDate) -> Boolean,
    onDateClicked: (LocalDate) -> Unit,
    onDismissButtonClicked: () -> Unit,
    onConfirmButtonClicked: () -> Unit,
) {
    Column(modifier = modifier) {
        DatePickerTitles(selectedDate = selectedDate)
        HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
        DatePickerCalendar(
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                top = 20.dp,
                bottom = 8.dp,
            ),
            selectedDate = selectedDate,
            onDateClicked = onDateClicked,
            dateIsEnabled = dateIsEnabled,
        )
        DialogButtons(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                )
                .align(Alignment.End)
                .padding(top = 2.dp, bottom = 12.dp),
            onDismissButtonClicked = onDismissButtonClicked,
            onConfirmButtonClicked = onConfirmButtonClicked,
        )
    }
}

@Composable
private fun DatePickerCalendarContentLandscape(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    dateIsEnabled: (LocalDate) -> Boolean,
    onDateClicked: (LocalDate) -> Unit,
    onDismissButtonClicked: () -> Unit,
    onConfirmButtonClicked: () -> Unit,
) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        DatePickerTitles(
            modifier = Modifier.weight(1f),
            selectedDate = selectedDate,
        )
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(DividerDefaults.Thickness)
                .background(color = DividerDefaults.color)
                .fillMaxHeight()
        )
        Column(
            modifier = Modifier
                .weight(3f)
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                )
        ) {
            DatePickerCalendar(
                modifier = Modifier
                    .padding(
                        top = 20.dp,
                        bottom = 8.dp
                    )
                    .height(250.dp),
                selectedDate = selectedDate,
                onDateClicked = onDateClicked,
                dateIsEnabled = dateIsEnabled,
            )
            DialogButtons(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp, bottom = 12.dp),
                onDismissButtonClicked = onDismissButtonClicked,
                onConfirmButtonClicked = onConfirmButtonClicked,
            )
        }
    }
}

@Composable
private fun DialogButtons(
    modifier: Modifier,
    onDismissButtonClicked: () -> Unit,
    onConfirmButtonClicked: () -> Unit,
) {
    Row(modifier = modifier) {
        TextButton(
            onClick = onDismissButtonClicked,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(text = stringResource(android.R.string.cancel))
        }
        TextButton(
            onClick = onConfirmButtonClicked,
            modifier = Modifier
        ) {
            Text(text = stringResource(android.R.string.ok))
        }
    }
}

@Composable
private fun DatePickerCalendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateClicked: (LocalDate) -> Unit,
    dateIsEnabled: (LocalDate) -> Boolean,
) {
    CalendarMonthlyPaged(
        modifier = modifier,
        initialMonth = selectedDate.yearMonth,
        firstDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek,
        onScrolledToNewMonth = { },
    ) { calendarDay ->
        val clickInteractionSource = remember { MutableInteractionSource() }

        val dayContainerSize = when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> 30.dp
            else -> 40.dp
        }

        val dateIsCurrentMonthDate = calendarDay.position !in notCurrentMonthDates
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(dayContainerSize)
                .aspectRatio(1f)
                .clip(shape = CircleShape)
                .background(
                    color = if (calendarDay.date == selectedDate && dateIsCurrentMonthDate) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                )
                .border(
                    border = BorderStroke(
                        color = if (calendarDay.date == LocalDate.now() && dateIsCurrentMonthDate) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                        width = 1.dp,
                    ),
                    shape = CircleShape,
                )
                .clickable(
                    onClick = { onDateClicked(calendarDay.date) },
                    interactionSource = clickInteractionSource,
                    indication = null,
                    enabled = calendarDay.position !in notCurrentMonthDates
                            && dateIsEnabled(calendarDay.date),
                )
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = calendarDay.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    calendarDay.position in notCurrentMonthDates -> Color.Transparent
                    calendarDay.date == selectedDate -> MaterialTheme.colorScheme.onPrimary
                    !dateIsEnabled(calendarDay.date) -> MaterialTheme.colorScheme.outlineVariant
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

private val notCurrentMonthDates = listOf(DayPosition.InDate, DayPosition.OutDate)

@Preview
@Composable
private fun DatePickerPortraitPreview() {
    DatePickerCalendarContentPortrait(
        selectedDate = LocalDate.now().plusDays(220),
        dateIsEnabled = { true },
        onDateClicked = { },
        onDismissButtonClicked = { },
        onConfirmButtonClicked = { },
    )
}

@Preview
@Composable
private fun DatePickerLandscapePreview() {
    DatePickerCalendarContentLandscape(
        selectedDate = LocalDate.now().plusDays(220),
        dateIsEnabled = { true },
        onDateClicked = { },
        onDismissButtonClicked = { },
        onConfirmButtonClicked = { },
    )
}