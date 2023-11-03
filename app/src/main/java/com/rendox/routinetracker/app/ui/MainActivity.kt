package com.rendox.routinetracker.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.rendox.routinetracker.core.model.StatusEntry
import com.rendox.routinetracker.core.ui.theme.RoutineTrackerTheme
import com.rendox.routinetracker.routine_stats.RoutineStatusCalendar
import com.rendox.routinetracker.routine_stats.routineStartDate
import com.rendox.routinetracker.routine_stats.statusList
import com.rendox.routinetracker.routine_stats.streakDates
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RoutineTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
//                            Card(
//                                modifier = Modifier
//                                    .padding(16.dp)
//                                    .wrapContentSize()
//                            ) {
                                RoutineStatusCalendar(
                                    currentMonth = YearMonth.of(2023, Month.NOVEMBER),
                                    firstDayOfWeek = DayOfWeek.MONDAY,
                                    routineStatuses = statusList.mapIndexed { dayNumber, status ->
                                        StatusEntry(
                                            date = routineStartDate.plus(DatePeriod(days = dayNumber)),
                                            status = status,
                                        )
                                    },
                                    streakDates = streakDates,
                                    today = LocalDate(2023, Month.NOVEMBER, 23)
                                )
//                            }
                        }
                    }
                }
            }
        }
    }
}