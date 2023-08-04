package com.rendox.routinetracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.rendox.routinetracker.ui.routine.RoutineItemScreen
import com.rendox.routinetracker.ui.theme.RoutineTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RoutineTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RoutineItemScreen(
                        title = "Do sports",
//                        imageId = R.drawable.cycling,
                        routineProgress = 0.25f,
                        description = "Stay fit and healthy Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim adz minim veniam, quis nostrud",
                        amountOfWorkToday = 4,
                        amountOfWorkTodayCompleted = 1,
                    )
                }
            }
        }
    }
}