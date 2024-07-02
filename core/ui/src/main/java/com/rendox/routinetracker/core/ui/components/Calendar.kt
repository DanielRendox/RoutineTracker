package com.rendox.routinetracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.ui.R
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun CalendarMonthlyPaged(
    modifier: Modifier = Modifier,
    initialMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    onScrolledToNewMonth: (month: YearMonth) -> Unit,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
) {
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek) }
    val startMonth = remember { initialMonth.minusMonths(100) }
    val endMonth = remember { initialMonth.plusMonths(100) }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = initialMonth,
        firstDayOfWeek = daysOfWeek.first(),
        outDateStyle = OutDateStyle.EndOfGrid,
    )

    LaunchedEffect(calendarState.firstVisibleMonth) {
        onScrolledToNewMonth(calendarState.firstVisibleMonth.yearMonth)
    }

    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier) {
        CalendarMonthTitle(
            modifier = Modifier.wrapContentHeight(),
            currentMonth = calendarState.firstVisibleMonth.yearMonth,
            navigateToPrevious = {
                val newMonth = calendarState.firstVisibleMonth.yearMonth.previousMonth
                coroutineScope.launch {
                    calendarState.animateScrollToMonth(newMonth)
                }
            },
            navigateToNext = {
                val newMonth = calendarState.firstVisibleMonth.yearMonth.nextMonth
                coroutineScope.launch {
                    calendarState.animateScrollToMonth(newMonth)
                }
            },
        )
        HorizontalCalendar(
            state = calendarState,
            dayContent = dayContent,
            monthHeader = {
                DaysOfWeekTitles(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    daysOfWeek = daysOfWeek,
                )
            },
        )
    }
}

@Composable
fun CalendarMonthTitle(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    navigateToPrevious: () -> Unit,
    navigateToNext: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CalendarNavigationIcon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(
                id = R.string.calendar_left_navigation_icon_content_description,
            ),
            onClick = navigateToPrevious,
        )

        val locale = LocalLocale.current
        val monthDisplayName = remember(currentMonth, locale) {
            /*
            The month's number should be displayed in the FULL_STANDALONE format (LLLL),
            however, for some reason, on Android, it displays month numbers instead.
            On the other hand, FULL (MMMM) format would return incorrect names in Slavic
            languages (e.g. in Ukrainian, it would return "жовтня", not "жовтень"). Luckily,
            this issue can be worked around by leveraging java.util.Date class, although it
            is ugly.
             */
            val someDateInMonth = currentMonth.atDay(3)
            val randomOffset = ZoneOffset.MIN // it doesn't matter
            val date =
                Date.from(someDateInMonth.atStartOfDay().toInstant(randomOffset))
            val fullStandaloneMonthNameFormatter = SimpleDateFormat("LLLL", locale)
            fullStandaloneMonthNameFormatter.format(date).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
            }
        }

        Text(
            text = "$monthDisplayName ${currentMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        CalendarNavigationIcon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(
                id = R.string.calendar_right_navigation_icon_content_description,
            ),
            onClick = navigateToNext,
        )
    }
}

@Composable
private fun CalendarNavigationIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun DaysOfWeekTitles(
    modifier: Modifier = Modifier,
    daysOfWeek: List<DayOfWeek>,
) {
    Row(
        modifier = modifier,
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                text = dayOfWeek.getDisplayName(TextStyle.NARROW, LocalLocale.current),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CalendarMonthlyPagedPreview() {
    Surface {
        CalendarMonthlyPaged(
            initialMonth = LocalDate.now().yearMonth,
            firstDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek,
            onScrolledToNewMonth = {},
        ) {
            ExampleCalendarDate(day = it)
        }
    }
}

@Composable
fun ExampleCalendarDate(
    modifier: Modifier = Modifier,
    day: CalendarDay,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .aspectRatio(1f)
            .clip(shape = CircleShape),
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = when (day.position) {
                DayPosition.InDate, DayPosition.OutDate -> MaterialTheme.colorScheme.outlineVariant
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}