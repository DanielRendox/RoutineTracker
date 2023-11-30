package com.rendox.routinetracker.add_routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.rendox.routinetracker.add_routine.choose_routine_type.ChooseRoutineTypePageState
import com.rendox.routinetracker.add_routine.choose_schedule.ChooseSchedulePageState
import com.rendox.routinetracker.add_routine.choose_routine_type.RoutineTypeUi
import com.rendox.routinetracker.add_routine.navigation.AddRoutineDestination
import com.rendox.routinetracker.add_routine.navigation.navigate
import com.rendox.routinetracker.add_routine.navigation.yesNoRoutineDestinations
import com.rendox.routinetracker.add_routine.set_goal.SetGoalPageState
import com.rendox.routinetracker.core.model.Routine
import com.rendox.routinetracker.core.ui.helpers.UiText
import com.rendox.routinetracker.feature.agenda.R

@Stable
class AddRoutineScreenState(
    val navController: NavHostController,
    val navBackStackEntry: NavBackStackEntry?,
    val chooseRoutineTypePageState: ChooseRoutineTypePageState,
    val setGoalPageState: SetGoalPageState,
    val chooseSchedulePageState: ChooseSchedulePageState,
    private val saveRoutine: (Routine) -> Unit,
    private val popOuterBackStack: () -> Unit,
) {
    var navDestinations by mutableStateOf(yesNoRoutineDestinations)
        private set

    var navigateBackButtonText: UiText by mutableStateOf(UiText.DynamicString(""))
        private set

    var navigateForwardButtonText: UiText by mutableStateOf(UiText.DynamicString(""))
        private set

    var currentScreenNumber: Int? by mutableStateOf(1)
        private set

    init {
        when (chooseRoutineTypePageState.routineType) {
            RoutineTypeUi.YesNoRoutine -> navDestinations = yesNoRoutineDestinations
            RoutineTypeUi.MeasurableRoutine -> TODO()
        }

        navigateBackButtonText = when (navBackStackEntry?.destination?.route) {
            navDestinations.first().route -> UiText.StringResource(
                resId = android.R.string.cancel
            )

            else -> UiText.StringResource(
                resId = R.string.navigate_back_button_text
            )
        }

        navigateForwardButtonText = when (navBackStackEntry?.destination?.route) {
            AddRoutineDestination.ChooseRoutineType.route -> UiText.DynamicString("")
            navDestinations.last().route ->
                UiText.StringResource(resId = R.string.save)

            else -> UiText.StringResource(resId = R.string.navigate_forward_button_text)
        }

        currentScreenNumber = navBackStackEntry?.let { backStack ->
            navDestinations.map { it.route }.indexOf(backStack.destination.route) + 1
        }
    }

    fun navigateBackOrCancel() {
        when (navBackStackEntry?.destination?.route) {
            navDestinations.first().route -> {
                popOuterBackStack()
            }

            null -> {}
            else -> {
                navController.popBackStack()
            }
        }
    }

    fun navigateForwardOrSave() {
        if (checkIfInputContainsErrors()) return

        when (val currentDestinationRoute = navBackStackEntry?.destination?.route) {
            navDestinations.last().route -> {
                saveRoutine(assembleRoutine())
                popOuterBackStack()
            }

            null -> {}

            else -> {
                val currentDestinationIndex =
                    navDestinations.map { it.route }.indexOf(currentDestinationRoute)
                val nextDestination = navDestinations[currentDestinationIndex + 1]
                navController.navigate(nextDestination)
            }
        }
    }

    private fun checkIfInputContainsErrors(): Boolean = when (navBackStackEntry?.destination?.route) {
        AddRoutineDestination.SetGoal.route -> setGoalPageState.checkIfContainsErrors()
        else -> false
    }

    private fun assembleRoutine(): Routine {
        TODO()
    }
}

@Composable
fun rememberAddRoutineScreenState(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry?,
    chooseRoutineTypePageState: ChooseRoutineTypePageState,
    setGoalPageState: SetGoalPageState,
    chooseSchedulePageState: ChooseSchedulePageState,
    saveRoutine: (Routine) -> Unit,
    popOuterBackStack: () -> Unit,
) = remember(
    navController,
    navBackStackEntry,
    chooseRoutineTypePageState,
    setGoalPageState,
    chooseSchedulePageState,
) {
    AddRoutineScreenState(
        navController = navController,
        navBackStackEntry = navBackStackEntry,
        chooseRoutineTypePageState = chooseRoutineTypePageState,
        setGoalPageState = setGoalPageState,
        chooseSchedulePageState = chooseSchedulePageState,
        saveRoutine = saveRoutine,
        popOuterBackStack = popOuterBackStack,
    )
}
