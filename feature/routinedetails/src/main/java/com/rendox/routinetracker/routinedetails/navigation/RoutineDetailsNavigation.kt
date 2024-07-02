package com.rendox.routinetracker.routinedetails.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rendox.routinetracker.routinedetails.RoutineDetailsRoute

const val ROUTINE_DETAILS_ROUTE = "routine_details"
internal const val ROUTINE_ID_ARG = "routineId"

fun NavController.navigateToRoutineDetails(
    routineId: Long,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {},
) {
    this.navigate("$ROUTINE_DETAILS_ROUTE/$routineId", navOptionsBuilder)
}

fun NavGraphBuilder.routineDetailsScreen(popBackStack: () -> Unit) {
    composable(
        route = "$ROUTINE_DETAILS_ROUTE/{$ROUTINE_ID_ARG}",
        arguments = listOf(
            navArgument(ROUTINE_ID_ARG) { type = NavType.LongType },
        ),
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
    ) {
        val routineId = it.arguments!!.getLong(ROUTINE_ID_ARG)
        RoutineDetailsRoute(
            navigateBack = popBackStack,
            routineId = routineId,
        )
    }
}