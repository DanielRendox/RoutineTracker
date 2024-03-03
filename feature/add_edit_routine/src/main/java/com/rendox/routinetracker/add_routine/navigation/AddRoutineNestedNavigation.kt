package com.rendox.routinetracker.add_routine.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rendox.routinetracker.add_routine.AddRoutineScreenState
import com.rendox.routinetracker.add_routine.choose_routine_type.ChooseRoutineTypePage
import com.rendox.routinetracker.add_routine.choose_schedule.ChooseSchedulePage
import com.rendox.routinetracker.add_routine.set_goal.SetGoalPage
import com.rendox.routinetracker.add_routine.tweak_routine.TweakRoutinePage
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.feature.agenda.R
import java.time.temporal.WeekFields

sealed class AddRoutineDestination(
    val route: String,
    @StringRes val screenTitleId: Int,
) {
    data object ChooseRoutineType : AddRoutineDestination(
        route = "choose_routine_type_route",
        screenTitleId = R.string.choose_routine_type_page_name,
    )

    data object SetGoal : AddRoutineDestination(
        route = "set_goal_route",
        screenTitleId = R.string.set_goal_page_name,
    )

    data object ChooseSchedule : AddRoutineDestination(
        route = "choose_schedule_route",
        screenTitleId = R.string.choose_schedule_page_name,
    )

    data object TweakRoutine : AddRoutineDestination(
        route = "tweak_habit_screen",
        screenTitleId = R.string.tweak_routine_page_name,
    )
}

val yesNoHabitDestinations = listOf(
    AddRoutineDestination.ChooseRoutineType,
    AddRoutineDestination.SetGoal,
    AddRoutineDestination.ChooseSchedule,
    AddRoutineDestination.TweakRoutine,
)

internal fun NavController.navigate(
    destination: AddRoutineDestination
) {
    navigate(destination.route)
}

@Composable
internal fun AddRoutineNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    addRoutineScreenState: AddRoutineScreenState,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = addRoutineScreenState.navDestinations.first().route,
        enterTransition = {
            slideInHorizontally { it }
        },
        exitTransition = {
            slideOutHorizontally { -it }
        },
        popEnterTransition = {
            slideInHorizontally { -it }
        },
        popExitTransition = {
            slideOutHorizontally { it }
        },
    ) {
        composable(route = AddRoutineDestination.ChooseRoutineType.route) {
            val startDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek

            ChooseRoutineTypePage(
                navigateForward = { addRoutineScreenState.navigateForwardOrSave(startDayOfWeek) },
                chooseRoutineTypePageState = addRoutineScreenState.chooseRoutineTypePageState,
            )
        }
        composable(route = AddRoutineDestination.SetGoal.route) {
            SetGoalPage(
                setGoalPageState = addRoutineScreenState.setGoalPageState
            )
        }
        composable(route = AddRoutineDestination.ChooseSchedule.route) {
            ChooseSchedulePage(
                chooseSchedulePageState = addRoutineScreenState.chooseSchedulePageState
            )
        }
        composable(route = AddRoutineDestination.TweakRoutine.route) {
            TweakRoutinePage(
                tweakRoutinePageState = addRoutineScreenState.tweakRoutinePageState,
            )
        }
    }
}