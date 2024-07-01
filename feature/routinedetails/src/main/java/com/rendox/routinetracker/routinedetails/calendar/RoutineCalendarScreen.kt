package com.rendox.routinetracker.routinedetails.calendar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.yearMonth
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.feature.routinedetails.R
import com.rendox.routinetracker.routinedetails.CalendarDateData
import java.time.YearMonth
import java.time.temporal.WeekFields
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn

@Composable
fun RoutineCalendarScreen(
    modifier: Modifier = Modifier,
    habit: Habit,
    routineCalendarDates: Map<LocalDate, CalendarDateData>,
    currentMonth: YearMonth,
    currentStreakDurationInDays: Int,
    longestStreakDurationInDays: Int,
    insertCompletion: (Habit.CompletionRecord) -> Unit,
    onScrolledToNewMonth: (YearMonth) -> Unit,
) {
    val currentStreakDurationInDaysString = pluralStringResource(
        id = com.rendox.routinetracker.core.ui.R.plurals.num_of_days,
        count = currentStreakDurationInDays,
    )
    val longestStreakDurationInDaysString = pluralStringResource(
        id = com.rendox.routinetracker.core.ui.R.plurals.num_of_days,
        count = longestStreakDurationInDays,
    )

    val onDateClick: (LocalDate) -> Unit = { date ->
        val numOfTimesCompleted = routineCalendarDates[date]?.numOfTimesCompleted
        if (numOfTimesCompleted != null) {
            when (habit) {
                is Habit.YesNoHabit -> {
                    val completion = Habit.YesNoHabit.CompletionRecord(
                        date = date,
                        numOfTimesCompleted = if (numOfTimesCompleted > 0F) 0F else 1F,
                    )
                    insertCompletion(completion)
                }
            }
        }
    }

    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            RoutineCalendarScreenLandscape(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                routineCalendarDates = routineCalendarDates,
                currentMonth = currentMonth,
                currentStreakDurationInDays = currentStreakDurationInDays,
                currentStreakDurationInDaysString = currentStreakDurationInDaysString,
                longestStreakDurationInDays = longestStreakDurationInDays,
                longestStreakDurationInDaysString = longestStreakDurationInDaysString,
                onScrolledToNewMonth = onScrolledToNewMonth,
                onDateClick = onDateClick,
            )
        }
        else -> {
            RoutineCalendarScreenPortrait(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp),
                routineCalendarDates = routineCalendarDates,
                currentMonth = currentMonth,
                currentStreakDurationInDays = currentStreakDurationInDays,
                currentStreakDurationInDaysString = currentStreakDurationInDaysString,
                longestStreakDurationInDays = longestStreakDurationInDays,
                longestStreakDurationInDaysString = longestStreakDurationInDaysString,
                onScrolledToNewMonth = onScrolledToNewMonth,
                onDateClick = onDateClick,
            )
        }
    }
}

@Composable
private fun RoutineCalendarScreenPortrait(
    modifier: Modifier = Modifier,
    routineCalendarDates: Map<LocalDate, CalendarDateData>,
    currentMonth: YearMonth,
    currentStreakDurationInDays: Int,
    currentStreakDurationInDaysString: String,
    longestStreakDurationInDays: Int,
    longestStreakDurationInDaysString: String,
    onScrolledToNewMonth: (YearMonth) -> Unit,
    onDateClick: (LocalDate) -> Unit,
) {
    Column(modifier = modifier) {
        RoutineCalendar(
            modifier = Modifier.padding(bottom = 24.dp),
            currentMonth = currentMonth,
            firstDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek,
            routineCalendarDates = routineCalendarDates,
            onDateClick = onDateClick,
            onScrolledToNewMonth = onScrolledToNewMonth,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LongestStreakCard(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                longestStreakDurationInDays = longestStreakDurationInDays,
                longestStreakDurationInDaysString = longestStreakDurationInDaysString,
            )
            Spacer(modifier = Modifier.width(16.dp))
            CurrentStreakCard(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                currentStreakDurationInDays = currentStreakDurationInDays,
                currentStreakDurationInDaysString = currentStreakDurationInDaysString,
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun RoutineCalendarScreenLandscape(
    modifier: Modifier = Modifier,
    routineCalendarDates: Map<LocalDate, CalendarDateData>,
    currentMonth: YearMonth,
    currentStreakDurationInDays: Int,
    currentStreakDurationInDaysString: String,
    longestStreakDurationInDays: Int,
    longestStreakDurationInDaysString: String,
    onScrolledToNewMonth: (YearMonth) -> Unit,
    onDateClick: (LocalDate) -> Unit,
) {
    Row(modifier = modifier) {
        RoutineCalendar(
            modifier = Modifier
                .weight(3f)
                .padding(end = 24.dp),
            currentMonth = currentMonth,
            firstDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek,
            routineCalendarDates = routineCalendarDates,
            onDateClick = onDateClick,
            onScrolledToNewMonth = onScrolledToNewMonth,
        )
        Column(modifier = Modifier.weight(2f)) {
            CurrentStreakCard(
                modifier = Modifier.fillMaxWidth(),
                currentStreakDurationInDays = currentStreakDurationInDays,
                currentStreakDurationInDaysString = currentStreakDurationInDaysString,
            )
            Spacer(modifier = Modifier.height(24.dp))
            LongestStreakCard(
                modifier = Modifier.fillMaxWidth(),
                longestStreakDurationInDays = longestStreakDurationInDays,
                longestStreakDurationInDaysString = longestStreakDurationInDaysString,
            )
        }
    }
}

@Composable
private fun CurrentStreakCard(
    modifier: Modifier = Modifier,
    currentStreakDurationInDays: Int,
    currentStreakDurationInDaysString: String,
) {
    RoutineStreakCard(
        modifier = modifier,
        icon = painterResource(R.drawable.baseline_commit_24),
        title = "$currentStreakDurationInDays $currentStreakDurationInDaysString",
        bodyText = stringResource(id = R.string.current_streak),
    )
}

@Composable
private fun LongestStreakCard(
    modifier: Modifier = Modifier,
    longestStreakDurationInDays: Int,
    longestStreakDurationInDaysString: String,
) {
    RoutineStreakCard(
        modifier = modifier,
        icon = painterResource(R.drawable.trophy_24),
        title = "$longestStreakDurationInDays $longestStreakDurationInDaysString",
        bodyText = stringResource(id = R.string.longest_streak),
    )
}

@Composable
private fun RoutineStreakCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    bodyText: String,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(24.dp),
                painter = icon,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = title,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:parent=pixel_5, orientation=landscape",
)
@Composable
private fun RoutineDetailsScreenPreview() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    Surface {
        RoutineCalendarScreen(
            habit = Habit.YesNoHabit(
                name = "Habit",
                schedule = Schedule.EveryDaySchedule(startDate = today),
            ),
            routineCalendarDates = emptyMap(),
            currentMonth = today.toJavaLocalDate().yearMonth,
            currentStreakDurationInDays = 15,
            longestStreakDurationInDays = 30,
            insertCompletion = {},
            onScrolledToNewMonth = {},
        )
    }
}