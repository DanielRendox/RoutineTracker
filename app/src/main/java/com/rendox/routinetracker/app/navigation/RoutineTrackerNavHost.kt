package com.rendox.routinetracker.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rendox.routinetracker.add_routine.navigation.addRoutineScreen
import com.rendox.routinetracker.add_routine.navigation.navigateToAddRoutine
import com.rendox.routinetracker.feature.agenda.navigation.agendaScreen
import com.rendox.routinetracker.routine_details.navigation.navigateToRoutineDetails
import com.rendox.routinetracker.routine_details.navigation.routineDetailsScreen

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
        addRoutineScreen(
            popBackStack = { navController.popBackStack() }
        )
        agendaScreen(
            onRoutineClick = {
                navController.navigateToRoutineDetails(routineId = it) { launchSingleTop = true }
            },
            onAddRoutineClick = { navController.navigateToAddRoutine() },
        )
        routineDetailsScreen (
            popBackStack = { navController.popBackStack() }
        )
    }
}