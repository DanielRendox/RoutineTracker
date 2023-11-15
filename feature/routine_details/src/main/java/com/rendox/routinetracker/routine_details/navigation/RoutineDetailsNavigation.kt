package com.rendox.routinetracker.routine_details.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rendox.routinetracker.routine_details.RoutineDetailsRoute

const val routineDetails = "routine_details"
internal const val routineIdArg = "routineId"

fun NavController.navigateToRoutineDetails(
    routineId: Long,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {},
) {
    this.navigate("$routineDetails/$routineId", navOptionsBuilder)
}

fun NavGraphBuilder.routineDetails(navigateToPreviousScreen: () -> Unit) {
    composable(
        route = "$routineDetails/{$routineIdArg}",
        arguments = listOf(
            navArgument(routineIdArg) { type = NavType.LongType }
        ),
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }
    ) {
        RoutineDetailsRoute(
            navigateToPreviousScreen = navigateToPreviousScreen,
            routineId = it.arguments!!.getLong(routineIdArg),
        )
    }
}