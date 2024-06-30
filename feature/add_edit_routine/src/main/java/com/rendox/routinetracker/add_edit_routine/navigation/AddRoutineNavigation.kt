package com.rendox.routinetracker.add_edit_routine.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.rendox.routinetracker.add_edit_routine.AddRoutineRoute

const val addRoutineRoute = "add_routine"

fun NavController.navigateToAddRoutine(navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}) {
    this.navigate(addRoutineRoute, navOptionsBuilder)
}

fun NavGraphBuilder.addRoutineScreen(
    navigateBackAndRecreate: () -> Unit,
    navigateBack: () -> Unit,
) {
    composable(
        route = addRoutineRoute,
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