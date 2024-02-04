package com.rendox.routinetracker.feature.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
fun RoutineTrackerWeekCalendar(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    initialDate: LocalDate,
    today: LocalDate,
    dateOnClick: (LocalDate) -> Unit
) {

    val startDate = remember { initialDate.minusDays(500) }
    val endDate = remember { initialDate.plusDays(500) }

    val calendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = initialDate,
        firstDayOfWeek = initialDate.minusDays(3).dayOfWeek,
    )

    WeekCalendar(
        modifier = modifier,
        state = calendarState,
        calendarScrollPaged = false,
        dayContent = {
            WeekCalendarDay(
                date = it.date,
                today = today,
                selected = selectedDate == it.date,
                onClick = dateOnClick,
            )
        }
    )
}

@Composable
private fun WeekCalendarDay(
    modifier: Modifier = Modifier,
    date: LocalDate,
    today: LocalDate,
    selected: Boolean,
    onClick: (LocalDate) -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val backgroundColor =
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    val foregroundColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceColorAtElevation(48.dp)
    val todayIndicatorColor =
        if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .width(48.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(25))
            .background(color = backgroundColor)
            .clickable { onClick(date) }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f),
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, LocalLocale.current),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
            )

            Surface(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxWidth(),
                color = foregroundColor,
                shape = RoundedCornerShape(15)
            ) {
                Box {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        if (date == today) {
            Box(
                modifier = Modifier
                    .padding(bottom = 1.dp)
                    .height(2.dp)
                    .width(screenWidth / 27f)
                    .clip(RoundedCornerShape(100))
                    .background(color = todayIndicatorColor)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(showSystemUi = false)
@Composable
private fun RoutineTrackerWeekCalendarPreview() {
    Surface {
        val today = LocalDate.now()
        var selectedDate by remember { mutableStateOf(today) }
        RoutineTrackerWeekCalendar(
            modifier = Modifier
                .height(70.dp)
                .fillMaxWidth(),
            initialDate = today,
            dateOnClick = { selectedDate = it },
            selectedDate = selectedDate,
            today = today,
        )
    }
}