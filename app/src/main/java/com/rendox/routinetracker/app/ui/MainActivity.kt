package com.rendox.routinetracker.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.app.ui.theme.RoutineTrackerTheme
import androidx.compose.runtime.getValue
import com.rendox.routinetracker.feature.routinedetails.RoutineDetailsScreen
import com.rendox.routinetracker.feature.routinedetails.RoutineViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

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
                    val routineId = 1
                    val viewModel by viewModel<RoutineViewModel> {
                        parametersOf(routineId)
                    }
                    val state by viewModel.routineScreenState.collectAsStateWithLifecycle()
                    RoutineDetailsScreen(
                        modifier = Modifier.fillMaxSize(),
                        routineScreenState = state,
                    )
                }
            }
        }
    }
}