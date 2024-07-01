package com.rendox.routinetracker.addeditroutine.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.rendox.routinetracker.addeditroutine.AddRoutineRoute

const val ADD_ROUTINE_ROUTE = "add_routine"

fun NavController.navigateToAddRoutine(navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}) {
    this.navigate(ADD_ROUTINE_ROUTE, navOptionsBuilder)
}

fun NavGraphBuilder.addRoutineScreen(
    navigateBackAndRecreate: () -> Unit,
    navigateBack: () -> Unit,
) {
    composable(
        route = ADD_ROUTINE_ROUTE,
        enterTransition = {
            fadeIn()
        },
        exitTransition = {
            fadeOut()
        },
    ) {
        AddRoutineRoute(
            navigateBackAndRecreate = navigateBackAndRecreate,
            navigateBack = navigateBack,
        )
    }
}