package com.rendox.routinetracker.routine_details.navigation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rendox.routinetracker.routine_details.RoutineDetailsRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
            navArgument(routineIdArg) { type = NavType.LongType }
        ),
        enterTransition = {
            fadeIn() + scaleIn(
                initialScale = 0.75f,
                animationSpec = TweenSpec(
                    durationMillis = 100,
                    easing = LinearEasing,
                )
            )
        },
        exitTransition = {
            fadeOut() + scaleOut(
                targetScale = 0.75f,
                animationSpec = TweenSpec(
                    durationMillis = 100,
                    easing = LinearEasing,
                )
            )
        }
    ) {
        val routineId = it.arguments!!.getLong(routineIdArg)
        RoutineDetailsRoute(
            popBackStack = popBackStack,
            routineId = routineId,
            routineDetailsViewModel = koinViewModel { parametersOf(routineId) }
        )
    }
}