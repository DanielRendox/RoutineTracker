package com.rendox.routinetracker.routine_details.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.ui.components.CalendarMonthlyPaged
import com.rendox.routinetracker.core.ui.theme.routineStatusColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.DayOfWeek
import java.time.YearMonth

@Composable
fun RoutineCalendar(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    routineCalendarDates: Map<LocalDate, CalendarDateData>,
    onDateClick: (date: LocalDate) -> Unit,
    onScrolledToNewMonth: (month: YearMonth) -> Unit,
) {
    CalendarMonthlyPaged(
        modifier = modifier,
        initialMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
        onScrolledToNewMonth = onScrolledToNewMonth,
        dayContent = { calendarDay ->
            val calendarDate: CalendarDateData? = remember(routineCalendarDates) {
                routineCalendarDates[calendarDay.date.toKotlinLocalDate()]
            }
            RoutineStatusDay(
                day = calendarDay,
                habitStatus = calendarDate?.status,
                includedInStreak = calendarDate?.includedInStreak ?: false,
                onClick = onDateClick,
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
    onClick: (date: LocalDate) -> Unit,
) {
    val completedStroke = MaterialTheme.routineStatusColors.completedStroke
    val completedBackground = MaterialTheme.routineStatusColors.completedBackground

    val failedStroke = MaterialTheme.routineStatusColors.failedStroke
    val failedBackground = MaterialTheme.routineStatusColors.failedBackground

    val skippedBackground =
        if (includedInStreak) MaterialTheme.routineStatusColors.completedBackgroundLight
        else MaterialTheme.routineStatusColors.failedBackgroundLight
    val skippedStroke =
        if (includedInStreak) MaterialTheme.routineStatusColors.completedStroke
        else MaterialTheme.routineStatusColors.failedStroke

    val backgroundColor: Color = when (day.position) {
        DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
        else -> when (habitStatus) {
            null -> Color.Transparent
            HabitStatus.Planned -> MaterialTheme.routineStatusColors.pending
            HabitStatus.Backlog -> MaterialTheme.routineStatusColors.pending
            HabitStatus.PastDateAlreadyCompleted -> skippedBackground
            HabitStatus.FutureDateAlreadyCompleted -> Color.Transparent
            HabitStatus.NotDue -> Color.Transparent
            HabitStatus.OnVacation -> MaterialTheme.routineStatusColors.vacationBackground
            HabitStatus.Failed -> failedBackground
            HabitStatus.Completed -> completedBackground
            HabitStatus.PartiallyCompleted -> completedBackground
            HabitStatus.OverCompleted -> completedBackground
            HabitStatus.SortedOutBacklog -> completedBackground
            HabitStatus.Skipped -> skippedBackground
            HabitStatus.CompletedLater -> skippedBackground
            HabitStatus.NotStarted -> Color.Transparent
            HabitStatus.Finished -> Color.Transparent
        }
    }

    val strokeColor: Color = when (day.position) {
        DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
        else -> when (habitStatus) {
            null -> Color.Transparent
            HabitStatus.Planned -> MaterialTheme.routineStatusColors.pendingStroke
            HabitStatus.Backlog -> MaterialTheme.routineStatusColors.pendingStroke
            HabitStatus.PastDateAlreadyCompleted -> skippedStroke
            HabitStatus.FutureDateAlreadyCompleted -> Color.Transparent
            HabitStatus.NotDue -> Color.Transparent
            HabitStatus.OnVacation -> MaterialTheme.routineStatusColors.vacationStroke
            HabitStatus.Failed -> failedStroke
            HabitStatus.Completed -> completedStroke
            HabitStatus.PartiallyCompleted -> completedStroke
            HabitStatus.OverCompleted -> completedStroke
            HabitStatus.SortedOutBacklog -> completedStroke
            HabitStatus.Skipped -> skippedStroke
            HabitStatus.CompletedLater -> skippedStroke
            HabitStatus.NotStarted -> Color.Transparent
            HabitStatus.Finished -> Color.Transparent
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .aspectRatio(1f)
            .clip(shape = CircleShape)
            .background(color = backgroundColor, shape = CircleShape)
            .border(border = BorderStroke(width = 2.dp, color = strokeColor), shape = CircleShape)
            .then(
                if (habitStatus == null) Modifier
                else Modifier.clickable { onClick(day.date.toKotlinLocalDate()) }
            )
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