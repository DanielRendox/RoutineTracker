package com.rendox.routinetracker.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.ui.theme.RoutineTrackerTheme
import com.rendox.routinetracker.feature.agenda.AgendaScreen
import java.time.LocalTime

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val routineNamesList = listOf(
            "Do sports",
            "Learn new English words and do the exercises and also do super cool homework",
            "Spend time outside",
            "Make my app",
        )

        val statusList = listOf<RoutineStatus>(
            HistoricalStatus.Completed,
            PlanningStatus.Planned,
            HistoricalStatus.NotCompleted,
            HistoricalStatus.CompletedLater,
        )

        val completionTimeList = listOf(
            LocalTime.of(9, 0),
            null,
            LocalTime.of(12, 30),
            LocalTime.of(17, 0)
        )

        setContent {
            RoutineTrackerTheme {
                AgendaScreen(
                    routineNamesList = routineNamesList,
                    statusList = statusList,
                    completionTimeList = completionTimeList,
                )
            }
        }
    }
}