package com.rendox.routinetracker.routinedetails.calendar

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.ui.components.CalendarMonthlyPaged
import com.rendox.routinetracker.core.ui.theme.routineStatusColors
import com.rendox.routinetracker.routinedetails.CalendarDateData
import java.time.DayOfWeek
import java.time.YearMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate

@Composable
fun RoutineCalendar(
    modifier: Modifier = Modifier,
    initialMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    routineCalendarDates: Map<LocalDate, CalendarDateData>,
    onDateClick: (date: LocalDate) -> Unit,
    onScrolledToNewMonth: (month: YearMonth) -> Unit,
) {
    CalendarMonthlyPaged(
        modifier = modifier,
        initialMonth = initialMonth,
        firstDayOfWeek = firstDayOfWeek,
        onScrolledToNewMonth = onScrolledToNewMonth,
        dayContent = { calendarDay ->
            val calendarDate: CalendarDateData? = remember(routineCalendarDates) {
                routineCalendarDates[calendarDay.date.toKotlinLocalDate()]
            }
            RoutineStatusDay(
                modifier = Modifier.align(Alignment.Center),
                day = calendarDay,
                habitStatus = calendarDate?.status,
                includedInStreak = calendarDate?.includedInStreak ?: false,
                onClick = onDateClick,
                isPastDate = calendarDate?.isPastDate ?: false,
            )
        },
    )
}

@Composable
private fun RoutineStatusDay(
    modifier: Modifier = Modifier,
    day: CalendarDay,
    habitStatus: HabitStatus?,
    includedInStreak: Boolean,
    isPastDate: Boolean,
    onClick: (date: LocalDate) -> Unit,
) {
    val completedStroke = MaterialTheme.routineStatusColors.completedStroke
    val completedBackground = MaterialTheme.routineStatusColors.completedBackground

    val failedStroke = MaterialTheme.routineStatusColors.failedStroke
    val failedBackground = MaterialTheme.routineStatusColors.failedBackground

    val skippedBackground =
        if (includedInStreak) {
            MaterialTheme.routineStatusColors.skippedInStreak
        } else {
            MaterialTheme.routineStatusColors.skippedOutOfStreak
        }
    val skippedStroke =
        if (includedInStreak) {
            MaterialTheme.routineStatusColors.completedStroke
        } else {
            MaterialTheme.routineStatusColors.failedStroke
        }

    val pendingColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)

    val backgroundColor: Color = when (day.position) {
        DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
        else -> when (habitStatus) {
            null -> Color.Transparent
            HabitStatus.Planned -> pendingColor
            HabitStatus.Backlog -> pendingColor
            HabitStatus.AlreadyCompleted -> if (isPastDate) skippedBackground else Color.Transparent
            HabitStatus.NotDue -> if (isPastDate) skippedBackground else Color.Transparent
            HabitStatus.OnVacation -> MaterialTheme.routineStatusColors.vacationBackground
            HabitStatus.Failed -> failedBackground
            HabitStatus.Completed -> completedBackground
            HabitStatus.PartiallyCompleted -> completedBackground
            HabitStatus.OverCompleted -> completedBackground
            HabitStatus.SortedOutBacklog -> completedBackground
            HabitStatus.CompletedLater -> skippedBackground
            HabitStatus.NotStarted -> Color.Transparent
            HabitStatus.Finished -> Color.Transparent
        }
    }

    val strokeColor: Color = when (day.position) {
        DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
        else -> when (habitStatus) {
            null -> Color.Transparent
            HabitStatus.Planned -> pendingColor
            HabitStatus.Backlog -> pendingColor
            HabitStatus.AlreadyCompleted -> if (isPastDate) skippedStroke else Color.Transparent
            HabitStatus.NotDue -> if (isPastDate) skippedStroke else Color.Transparent
            HabitStatus.OnVacation -> MaterialTheme.routineStatusColors.vacationStroke
            HabitStatus.Failed -> failedStroke
            HabitStatus.Completed -> completedStroke
            HabitStatus.PartiallyCompleted -> completedStroke
            HabitStatus.OverCompleted -> completedStroke
            HabitStatus.SortedOutBacklog -> completedStroke
            HabitStatus.CompletedLater -> skippedStroke
            HabitStatus.NotStarted -> Color.Transparent
            HabitStatus.Finished -> Color.Transparent
        }
    }

    val verticalPadding: Dp
    val dayContainerSize: Dp
    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            verticalPadding = 2.dp
            dayContainerSize = 30.dp
        }
        else -> {
            verticalPadding = 4.dp
            dayContainerSize = 40.dp
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 7.dp, vertical = verticalPadding)
            .size(dayContainerSize)
            .aspectRatio(1f)
            .clip(shape = CircleShape)
            .background(color = backgroundColor, shape = CircleShape)
            .border(border = BorderStroke(width = 2.dp, color = strokeColor), shape = CircleShape)
            .then(
                if (habitStatus == null ||
                    day.position in arrayOf(
                        DayPosition.InDate,
                        DayPosition.OutDate,
                    )
                ) {
                    Modifier
                } else {
                    Modifier.clickable { onClick(day.date.toKotlinLocalDate()) }
                },
            ),
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when (day.position) {
                DayPosition.InDate, DayPosition.OutDate -> MaterialTheme.colorScheme.outlineVariant
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}