package com.rendox.routinetracker.routine_details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val calendarNavRoute = "routine_details_calendar"

internal fun NavController.navigateToCalendar(navOptions: NavOptions? = null) {
    this.navigate(calendarNavRoute, navOptions)
}

internal fun NavGraphBuilder.calendarScreen(routineId: Long) {
    composable(
        route = calendarNavRoute,
        arguments = listOf(
            navArgument(routineIdArg) { type = NavType.LongType },
        )
    ) {
        RoutineCalendarRoute(
            viewModel = koinViewModel { parametersOf(routineId) }
        )
    }
}