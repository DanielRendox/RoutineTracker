package com.rendox.routinetracker.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val useDarkTheme = isSystemInDarkTheme()

            RoutineTrackerTheme(
                useDarkTheme = useDarkTheme,
                disableDynamicColor = false,
            ) {
                RoutineTrackerNavHost(
                    modifier = Modifier.fillMaxSize(),
                    startDestination = agendaNavRoute,
                )
            }
        }
    }
}