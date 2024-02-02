package com.rendox.routinetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.rendox.routinetracker.app.navigation.RoutineTrackerNavHost
import com.rendox.routinetracker.core.ui.theme.RoutineTrackerTheme
import com.rendox.routinetracker.feature.agenda.navigation.agendaNavRoute

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        setContent {
            val useDarkTheme = isSystemInDarkTheme()
            windowInsetsController.isAppearanceLightStatusBars = !useDarkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !useDarkTheme

            RoutineTrackerTheme(useDarkTheme = useDarkTheme) {
                RoutineTrackerNavHost(
                    modifier = Modifier.fillMaxSize(),
                    startDestination = agendaNavRoute,
                )
            }
        }
    }
}