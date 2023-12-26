package com.rendox.routinetracker.routine_details.calendar

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.feature.routine_details.R
import java.time.YearMonth
import java.time.temporal.WeekFields

@Composable
internal fun RoutineCalendarRoute(
    modifier: Modifier = Modifier,
    viewModel: RoutineCalendarViewModel,
) {
    val habit by viewModel.habitFlow.collectAsStateWithLifecycle()
    val routineCalendarDates by viewModel.visibleDatesFlow.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonthFlow.collectAsStateWithLifecycle()
    val currentStreakDurationInDays by viewModel.currentStreakDurationInDays.collectAsStateWithLifecycle()
    val longestStreakDurationInDays by viewModel.longestStreakDurationInDays.collectAsStateWithLifecycle()

    habit?.let {
        RoutineCalendarScreen(
            modifier = modifier,
            habit = it,
            routineCalendarDates = routineCalendarDates,
            currentMonth = currentMonth,
            currentStreakDurationInDays = currentStreakDurationInDays,
            longestStreakDurationInDays = longestStreakDurationInDays,
            insertCompletion = { completionRecord ->
                viewModel.onHabitComplete(completionRecord)
            },
            onScrolledToNewMonth = { newMonth ->
                viewModel.onScrolledToNewMonth(newMonth)
            },
        )
    }
}

@Composable
fun RoutineCalendarScreen(
    modifier: Modifier = Modifier,
    habit: Habit,
    routineCalendarDates: List<RoutineCalendarDate>,
    currentMonth: YearMonth,
    currentStreakDurationInDays: Int,
    longestStreakDurationInDays: Int,
    insertCompletion: (Habit.CompletionRecord) -> Unit,
    onScrolledToNewMonth: (YearMonth) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {
        RoutineCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            currentMonth = currentMonth,
            firstDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek,
            routineCalendarDates = routineCalendarDates,
            onDateClick = { date ->
                val numOfTimesCompleted =
                    routineCalendarDates.find { it.date == date }?.numOfTimesCompleted
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
            },
            onScrolledToNewMonth = onScrolledToNewMonth,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val currentStreakDurationInDaysString = pluralStringResource(
                id = com.rendox.routinetracker.core.ui.R.plurals.num_of_days,
                count = currentStreakDurationInDays,
            )
            val longestStreakDurationInDaysString = pluralStringResource(
                id = com.rendox.routinetracker.core.ui.
                R.plurals.num_of_days,
                count = longestStreakDurationInDays,
            )

            RoutineStreakCard(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(top = 24.dp),
                icon = painterResource(R.drawable.baseline_commit_24),
                title = "$currentStreakDurationInDays $currentStreakDurationInDaysString",
                bodyText = stringResource(id = R.string.current_streak)
            )
            Spacer(modifier = Modifier.width(16.dp))
            RoutineStreakCard(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(top = 24.dp),
                icon = painterResource(R.drawable.trophy_24),
                title = "$longestStreakDurationInDays $longestStreakDurationInDaysString",
                bodyText = stringResource(id = R.string.longest_streak)
            )
        }
    }
}

@Composable
private fun RoutineStreakCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    bodyText: String,
) {
    Card(modifier = modifier) {
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

//@Preview
//@Composable
//private fun RoutineDetailsScreenPreview() {
//    Surface {
//        RoutineCalendarScreen(
//            uiState = CalendarScreenUiState(
//                currentMonth = YearMonth.of(2023, Month.NOVEMBER),
//                firstDayOfWeek = DayOfWeek.MONDAY,
//                routineStatuses = statusList.mapIndexed { dayNumber, status ->
//                    StatusEntry(
//                        date = routineStartDate.plus(DatePeriod(days = dayNumber)),
//                        status = status,
//                    )
//                },
//                streakDates = streakDates,
//                today = LocalDate(2023, Month.NOVEMBER, 23),
//                currentStreakDurationInDays = 10,
//                longestStreakDurationInDays = 50,
//            )
//        )
//    }
//}