package com.rendox.routinetracker.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rendox.routinetracker.addeditroutine.navigation.addRoutineScreen
import com.rendox.routinetracker.addeditroutine.navigation.navigateToAddRoutine
import com.rendox.routinetracker.feature.agenda.navigation.AGENDA_NAV_ROUTE
import com.rendox.routinetracker.feature.agenda.navigation.agendaScreen
import com.rendox.routinetracker.feature.agenda.navigation.navigateToAgenda
import com.rendox.routinetracker.routinedetails.navigation.navigateToRoutineDetails
import com.rendox.routinetracker.routinedetails.navigation.routineDetailsScreen

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
            navigateBackAndRecreate = {
                navController.navigateToAgenda {
                    popUpTo(route = AGENDA_NAV_ROUTE) { inclusive = true }
                }
            },
            navigateBack = {
                navController.navigateUp()
            },
        )
        agendaScreen(
            onRoutineClick = {
                navController.navigateToRoutineDetails(routineId = it) {
                    launchSingleTop = true
                }
            },
            onAddRoutineClick = { navController.navigateToAddRoutine() },
        )
        routineDetailsScreen(
            popBackStack = {
                navController.navigateToAgenda {
                    popUpTo(route = AGENDA_NAV_ROUTE) { inclusive = true }
                }
            },
        )
    }
}