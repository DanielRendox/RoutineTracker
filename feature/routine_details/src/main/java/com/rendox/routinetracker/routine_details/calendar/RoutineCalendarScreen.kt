package com.rendox.routinetracker.routine_details.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.feature.routine_details.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth

@Composable
fun RoutineCalendarScreen(
    modifier: Modifier = Modifier,
    uiState: CalendarScreenUiState,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {
        RoutineCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            currentMonth = uiState.currentMonth,
            firstDayOfWeek = uiState.firstDayOfWeek,
            routineStatuses = uiState.routineStatuses,
            streakDates = uiState.streakDates,
            today = uiState.today,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            uiState.currentStreakDurationInDays?.let {
                RoutineStreakCard(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 24.dp),
                    icon = painterResource(R.drawable.baseline_commit_24),
                    title = pluralStringResource(
                        id = R.plurals.num_of_days, count = it, it,
                    ),
                    bodyText = stringResource(id = R.string.current_streak)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            uiState.longestStreakDurationInDays?.let {
                RoutineStreakCard(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 24.dp),
                    icon = painterResource(R.drawable.trophy_24),
                    title = pluralStringResource(
                        id = R.plurals.num_of_days, count = it, it
                    ),
                    bodyText = stringResource(id = R.string.longest_streak)
                )
            }
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

@Preview
@Composable
private fun RoutineDetailsScreenPreview() {
    Surface {
        RoutineCalendarScreen(
            uiState = CalendarScreenUiState(
                currentMonth = YearMonth.of(2023, Month.NOVEMBER),
                firstDayOfWeek = DayOfWeek.MONDAY,
                routineStatuses = statusList.mapIndexed { dayNumber, status ->
                    StatusEntry(
                        date = routineStartDate.plus(DatePeriod(days = dayNumber)),
                        status = status,
                    )
                },
                streakDates = streakDates,
                today = LocalDate(2023, Month.NOVEMBER, 23),
                currentStreakDurationInDays = 10,
                longestStreakDurationInDays = 50,
            )
        )
    }
}