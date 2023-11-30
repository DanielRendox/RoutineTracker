package com.rendox.routinetracker.add_routine.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.rendox.routinetracker.add_routine.AddRoutineRoute

const val addRoutineRoute = "add_routine"

fun NavController.navigateToAddRoutine(
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
    ) {
    this.navigate(addRoutineRoute, navOptionsBuilder)
}

fun NavGraphBuilder.addRoutineScreen(popBackStack: () -> Unit) {
    composable(
        route = addRoutineRoute,
    ) {
        AddRoutineRoute(popBackStack = popBackStack)
    }
}