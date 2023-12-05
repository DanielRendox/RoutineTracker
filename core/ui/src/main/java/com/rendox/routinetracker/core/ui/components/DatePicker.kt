package com.rendox.routinetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.ui.R
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleDatePickerDialog(
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    initialDate: LocalDate = today,
    dismissButtonOnClick: () -> Unit,
    confirmButtonOnClick: (LocalDate) -> Unit,
) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = { dismissButtonOnClick() },
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 36.dp),
                    text = stringResource(id = R.string.date_picker_supporting_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = selectedDate.format(DateTimeFormatter.ofPattern("E, MMM d")),
                    style = MaterialTheme.typography.headlineMedium,
                )

                Divider(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))

                DatePickerCalendar(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 20.dp,
                        bottom = 8.dp
                    ),
                    selectedDate = selectedDate,
                    onDateClicked = { selectedDate = it },
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 12.dp)
                ) {
                    TextButton(
                        onClick = dismissButtonOnClick,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                    TextButton(
                        onClick = { confirmButtonOnClick(selectedDate) },
                        modifier = Modifier
                    ) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerCalendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateClicked: (LocalDate) -> Unit,
) {
    CalendarMonthlyPaged(
        modifier = modifier,
        initialMonth = selectedDate.yearMonth,
        firstDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek,
        onScrolledToNewMonth = { },
    ) { calendarDay ->
        val clickInteractionSource = remember { MutableInteractionSource() }
        if (calendarDay.position !in notCurrentMonthDates) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .clip(shape = CircleShape)
                    .background(
                        color = if (calendarDay.date == selectedDate) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        border = BorderStroke(
                            color = if (calendarDay.date == LocalDate.now()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                            width = 1.dp,
                        ),
                        shape = CircleShape,
                    )
                    .clickable(interactionSource = clickInteractionSource, indication = null) {
                        onDateClicked(calendarDay.date)
                    }
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = calendarDay.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (calendarDay.date == selectedDate) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}

private val notCurrentMonthDates = listOf(DayPosition.InDate, DayPosition.OutDate)

@Preview
@Composable
private fun DatePickerPreview() {
    Card(modifier = Modifier.width(300.dp)) {
        DatePickerCalendar(
            selectedDate = LocalDate.now().plusDays(10),
            onDateClicked = {}
        )
    }
}