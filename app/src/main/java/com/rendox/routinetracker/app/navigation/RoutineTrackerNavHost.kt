package com.rendox.routinetracker.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rendox.routinetracker.feature.agenda.navigation.agendaScreen
import com.rendox.routinetracker.feature.agenda.navigation.navigateToAgenda
import com.rendox.routinetracker.routine_details.navigation.navigateToRoutineDetails
import com.rendox.routinetracker.routine_details.navigation.routineDetails

@Composable
fun RoutineTrackerNavHost(
    modifier: Modifier = Modifier,
    startDestination: String,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        agendaScreen(
            onRoutineClick = {
                navController.navigateToRoutineDetails(routineId = it) { launchSingleTop = true }
            },
            onAddRoutineClick = { TODO() },
        )
        routineDetails(
            navigateToPreviousScreen = { navController.popBackStack() }
        )
    }
}