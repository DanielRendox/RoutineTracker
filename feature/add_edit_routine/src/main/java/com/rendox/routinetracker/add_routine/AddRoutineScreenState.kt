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
import com.rendox.routinetracker.add_routine.choose_routine_type.RoutineTypeUi
import com.rendox.routinetracker.add_routine.choose_schedule.ChooseSchedulePageState
import com.rendox.routinetracker.add_routine.choose_schedule.assembleSchedule
import com.rendox.routinetracker.add_routine.navigation.AddRoutineDestination
import com.rendox.routinetracker.add_routine.navigation.navigate
import com.rendox.routinetracker.add_routine.navigation.yesNoHabitDestinations
import com.rendox.routinetracker.add_routine.set_goal.SetGoalPageState
import com.rendox.routinetracker.add_routine.tweak_routine.TweakRoutinePageState
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.ui.R
import com.rendox.routinetracker.core.ui.helpers.UiText
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.toKotlinLocalTime

@Stable
class AddRoutineScreenState(
    val navController: NavHostController,
    val navBackStackEntry: NavBackStackEntry?,
    chooseRoutineTypePageState: ChooseRoutineTypePageState,
    setGoalPageState: SetGoalPageState,
    chooseSchedulePageState: ChooseSchedulePageState,
    tweakRoutinePageState: TweakRoutinePageState,
    private val saveRoutine: (Habit) -> Unit,
    private val navigateBackAndRecreate: () -> Unit,
    private val navigateBack: () -> Unit,
) {
    var chooseRoutineTypePageState by mutableStateOf(chooseRoutineTypePageState)
        private set

    var setGoalPageState by mutableStateOf(setGoalPageState)
        private set

    var chooseSchedulePageState by mutableStateOf(chooseSchedulePageState)
        private set

    var tweakRoutinePageState by mutableStateOf(tweakRoutinePageState)
        private set

    var navDestinations by mutableStateOf(yesNoHabitDestinations)
        private set

    var navigateBackButtonText: UiText by mutableStateOf(UiText.DynamicString(""))
        private set

    var navigateForwardButtonText: UiText by mutableStateOf(UiText.DynamicString(""))
        private set

    var currentScreenNumber: Int? by mutableStateOf(1)
        private set

    init {
        updateNavDestinations(chooseRoutineTypePageState.routineType)

        navigateBackButtonText = when (navBackStackEntry?.destination?.route) {
            navDestinations.first().route -> UiText.StringResource(resId = android.R.string.cancel)
            else -> UiText.StringResource(resId = R.string.back)
        }

        navigateForwardButtonText = when (navBackStackEntry?.destination?.route) {
            AddRoutineDestination.ChooseRoutineType.route -> UiText.DynamicString("")
            navDestinations.last().route -> UiText.StringResource(resId = R.string.save)
            else -> UiText.StringResource(resId = R.string.next)
        }

        currentScreenNumber = navBackStackEntry?.let { backStack ->
            navDestinations.map { it.route }.indexOf(backStack.destination.route) + 1
        }
    }

    val containsError: Boolean
        get() {
            val currentRoute = navBackStackEntry?.destination?.route
            return currentRoute == AddRoutineDestination.ChooseRoutineType.route
                    || (currentRoute == AddRoutineDestination.SetGoal.route && setGoalPageState.containsError)
                    || (currentRoute == AddRoutineDestination.ChooseSchedule.route && chooseSchedulePageState.containsError)
                    || (currentRoute == AddRoutineDestination.TweakRoutine.route && tweakRoutinePageState.containsError)
        }

    fun navigateBackOrCancel() {
        when (navBackStackEntry?.destination?.route) {
            navDestinations.first().route -> navigateBack()
            null -> {}
            else -> navController.popBackStack()
        }
    }

    fun navigateForwardOrSave(startDayOfWeek: DayOfWeek) {
        val currentDestinationRoute = navBackStackEntry?.destination?.route ?: return

        if (currentDestinationRoute == navDestinations.last().route) {
            val routine = assembleRoutine(startDayOfWeek)
            saveRoutine(routine)
            navigateBackAndRecreate()
            return
        }

        val currentDestinationIndex =
            navDestinations.map { it.route }.indexOf(currentDestinationRoute)
        val nextDestination = navDestinations[currentDestinationIndex + 1]

        if (currentDestinationRoute == AddRoutineDestination.SetGoal.route) {
            setGoalPageState.triggerErrorsIfAny()
            if (setGoalPageState.containsError) return
        }

        if (currentDestinationRoute == AddRoutineDestination.ChooseSchedule.route) {
            chooseSchedulePageState.triggerErrorsIfAny()
            if (chooseSchedulePageState.containsError) return
        }

        if (currentDestinationRoute == AddRoutineDestination.ChooseRoutineType.route) {
            updateNavDestinations(chooseRoutineTypePageState.routineType)
        }

        if (nextDestination == AddRoutineDestination.TweakRoutine) {
            tweakRoutinePageState.updateChosenSchedule(
                chooseSchedulePageState.selectedSchedulePickerState.assembleSchedule(
                    startDayOfWeek = startDayOfWeek,
                )
            )
        }

        navController.navigate(nextDestination)
    }

    private fun updateNavDestinations(routineType: RoutineTypeUi) {
        navDestinations = when (routineType) {
            RoutineTypeUi.YesNoHabit -> yesNoHabitDestinations
            RoutineTypeUi.MeasurableHabit -> TODO()
        }
    }

    private fun assembleRoutine(startDayOfWeek: DayOfWeek): Habit = when (chooseRoutineTypePageState.routineType) {
        is RoutineTypeUi.YesNoHabit -> Habit.YesNoHabit(
            name = setGoalPageState.routineName,
            description = setGoalPageState.routineDescription,
            schedule = chooseSchedulePageState.selectedSchedulePickerState.assembleSchedule(
                tweakRoutinePageState = tweakRoutinePageState,
                startDayOfWeek = startDayOfWeek,
            ),
            sessionDurationMinutes = tweakRoutinePageState.sessionDuration?.toMinutes()?.toInt(),
            defaultCompletionTime = tweakRoutinePageState.sessionTime?.toKotlinLocalTime(),
        )

        is RoutineTypeUi.MeasurableHabit -> TODO()
    }
}

@Composable
fun rememberAddRoutineScreenState(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry?,
    chooseRoutineTypePageState: ChooseRoutineTypePageState,
    setGoalPageState: SetGoalPageState,
    chooseSchedulePageState: ChooseSchedulePageState,
    tweakRoutinePageState: TweakRoutinePageState,
    saveRoutine: (Habit) -> Unit,
    navigateBackAndRecreate: () -> Unit,
    navigateBack: () -> Unit,
) = remember(
    navController,
    navBackStackEntry,
    chooseRoutineTypePageState,
    setGoalPageState,
    chooseSchedulePageState,
    tweakRoutinePageState,
) {
    AddRoutineScreenState(
        navController = navController,
        navBackStackEntry = navBackStackEntry,
        chooseRoutineTypePageState = chooseRoutineTypePageState,
        setGoalPageState = setGoalPageState,
        chooseSchedulePageState = chooseSchedulePageState,
        tweakRoutinePageState = tweakRoutinePageState,
        navigateBackAndRecreate = navigateBackAndRecreate,
        saveRoutine = saveRoutine,
        navigateBack = navigateBack,
    )
}
