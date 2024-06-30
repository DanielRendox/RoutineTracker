package com.rendox.routinetracker.routine_details.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rendox.routinetracker.routine_details.RoutineDetailsRoute

const val routineDetailsRoute = "routine_details"
internal const val routineIdArg = "routineId"

fun NavController.navigateToRoutineDetails(
    routineId: Long,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {},
) {
    this.navigate("$routineDetailsRoute/$routineId", navOptionsBuilder)
}

fun NavGraphBuilder.routineDetailsScreen(popBackStack: () -> Unit) {
    composable(
        route = "$routineDetailsRoute/{$routineIdArg}",
        arguments = listOf(
            navArgument(routineIdArg) { type = NavType.LongType },
        ),
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
    ) {
        val routineId = it.arguments!!.getLong(routineIdArg)
        RoutineDetailsRoute(
            navigateBack = popBackStack,
            routineId = routineId,
        )
    }
}