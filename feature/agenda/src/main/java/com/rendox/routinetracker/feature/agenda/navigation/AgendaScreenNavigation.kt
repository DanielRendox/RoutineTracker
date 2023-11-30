package com.rendox.routinetracker.feature.agenda.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.rendox.routinetracker.feature.agenda.AgendaRoute

const val agendaNavRoute = "agenda_route"

fun NavController.navigateToAgenda(
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {},
) {
    this.navigate(agendaNavRoute, navOptionsBuilder)
}

fun NavGraphBuilder.agendaScreen(
    onRoutineClick: (Long) -> Unit,
    onAddRoutineClick: () -> Unit,
) {
    composable(route = agendaNavRoute) {
        AgendaRoute(
            onRoutineClick = onRoutineClick,
            onAddRoutineClick = onAddRoutineClick,
        )
    }
}