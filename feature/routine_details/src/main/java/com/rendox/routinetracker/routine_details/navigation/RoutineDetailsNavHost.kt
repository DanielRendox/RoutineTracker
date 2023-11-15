package com.rendox.routinetracker.routine_details.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
internal fun RoutineDetailsNavHost(
    modifier: Modifier = Modifier,
    startDestination: String,
    routineId: Long,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        calendarScreen(routineId)
    }
}