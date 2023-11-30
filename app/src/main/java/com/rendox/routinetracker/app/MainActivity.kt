package com.rendox.routinetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rendox.routinetracker.app.navigation.RoutineTrackerNavHost
import com.rendox.routinetracker.core.ui.theme.RoutineTrackerTheme
import com.rendox.routinetracker.feature.agenda.navigation.agendaNavRoute

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO configure window insets
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            RoutineTrackerTheme {
                RoutineTrackerNavHost(startDestination = agendaNavRoute)
            }
        }
    }
}