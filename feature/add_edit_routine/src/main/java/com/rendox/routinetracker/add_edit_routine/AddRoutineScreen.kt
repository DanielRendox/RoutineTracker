package com.rendox.routinetracker.add_edit_routine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rendox.routinetracker.add_edit_routine.choose_routine_type.rememberChooseRoutineTypePageState
import com.rendox.routinetracker.add_edit_routine.choose_schedule.rememberChooseSchedulePageState
import com.rendox.routinetracker.add_edit_routine.choose_schedule.schedule_pickers.ScheduleTypeUi
import com.rendox.routinetracker.add_edit_routine.navigation.AddRoutineDestination
import com.rendox.routinetracker.add_edit_routine.navigation.AddRoutineNavHost
import com.rendox.routinetracker.add_edit_routine.set_goal.rememberSetGoalPageState
import com.rendox.routinetracker.add_edit_routine.tweak_routine.rememberTweakRoutinePageState
import com.rendox.routinetracker.core.domain.di.InsertHabitUseCase
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.core.ui.helpers.ObserveUiEvent
import com.rendox.routinetracker.feature.add_edit_routine.R
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.time.temporal.WeekFields

@Composable
internal fun AddRoutineRoute(
    modifier: Modifier = Modifier,
    navigateBackAndRecreate: () -> Unit,
    navigateBack: () -> Unit,
    insertHabit: InsertHabitUseCase = koinInject()
) {
    var dialogIsVisible by rememberSaveable { mutableStateOf(false) }
    DiscardCreatingNewHabitDialog(
        dialogIsVisible = dialogIsVisible,
        onDismissRequest = { dialogIsVisible = false },
        onConfirmButtonClicked = {
            dialogIsVisible = false
            navigateBack()
        },
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val coroutineScope = rememberCoroutineScope()
    val tweakRoutinePageState = rememberTweakRoutinePageState()
    val addRoutineScreenState = rememberAddRoutineScreenState(
        navController = navController,
        navBackStackEntry = navBackStackEntry,
        chooseRoutineTypePageState = rememberChooseRoutineTypePageState(),
        setGoalPageState = rememberSetGoalPageState(),
        chooseSchedulePageState = rememberChooseSchedulePageState(),
        tweakRoutinePageState = tweakRoutinePageState,
        navigateBack = navigateBack,
        saveRoutine = { routine ->
            coroutineScope.launch {
                insertHabit(routine)
                navigateBackAndRecreate()
            }
        },
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val newSchedule = tweakRoutinePageState.scheduleConvertedEvent?.data
    val scheduleConvertedMessage = stringResource(id = R.string.schedule_converted_snackbar_message)
    val newScheduleDisplayName = when (newSchedule) {
        is Schedule.EveryDaySchedule -> stringResource(ScheduleTypeUi.EveryDaySchedule.titleId)
        is Schedule.WeeklySchedule -> stringResource(ScheduleTypeUi.WeeklySchedule.titleId)
        is Schedule.MonthlySchedule -> stringResource(ScheduleTypeUi.MonthlySchedule.titleId)
        is Schedule.AlternateDaysSchedule -> stringResource(ScheduleTypeUi.AlternateDaysSchedule.titleId)
        else -> ""
    }

    ObserveUiEvent(tweakRoutinePageState.scheduleConvertedEvent) {
        snackbarHostState.showSnackbar(
            message = "$scheduleConvertedMessage $newScheduleDisplayName"
        )
    }


    AddRoutineScreen(
        modifier = modifier,
        addRoutineScreenState = addRoutineScreenState,
        snackbarHostState = snackbarHostState,
    )

    BackHandler {
       dialogIsVisible = true
    }
}

@Composable
private fun DiscardCreatingNewHabitDialog(
    modifier: Modifier = Modifier,
    dialogIsVisible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: () -> Unit,
) {
    if (dialogIsVisible) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            text = {
                Text(
                    text = stringResource(id = R.string.discard_habit_creation_dialog_title),
                )
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = stringResource(id = android.R.string.cancel),
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(onClick = onConfirmButtonClicked) {
                    Text(
                        text = stringResource(id = R.string.discard_habit_creation),
                    )
                }
            },
        )
    }
}

@Composable
internal fun AddRoutineScreen(
    modifier: Modifier = Modifier,
    addRoutineScreenState: AddRoutineScreenState,
    snackbarHostState: SnackbarHostState,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            AddRoutineNavHost(
                modifier = Modifier
                    .weight(1f),
                navController = addRoutineScreenState.navController,
                addRoutineScreenState = addRoutineScreenState,
            )

            val navigateBackButtonText =
                addRoutineScreenState.navigateBackButtonText.asString().uppercase()

            val navigateForwardButtonText =
                addRoutineScreenState.navigateForwardButtonText.asString().uppercase()

            val startDayOfWeek = WeekFields.of(LocalLocale.current).firstDayOfWeek

            SnackbarHost(hostState = snackbarHostState)

            AddRoutineBottomNavigation(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                navigateBackButtonText = navigateBackButtonText,
                navigateForwardButtonText = navigateForwardButtonText,
                navigateForwardButtonIsEnabled = !addRoutineScreenState.containsError,
                currentScreenNumber = addRoutineScreenState.currentScreenNumber,
                numOfScreens = addRoutineScreenState.navDestinations.size,
                navigateBackButtonOnClick = addRoutineScreenState::navigateBackOrCancel,
                navigateForwardButtonOnClick = {
                    addRoutineScreenState.navigateForwardOrSave(startDayOfWeek = startDayOfWeek)
                },
            )
        }
    }
}

@Composable
private fun AddRoutineBottomNavigation(
    modifier: Modifier = Modifier,
    navigateBackButtonText: String,
    navigateForwardButtonText: String,
    navigateForwardButtonIsEnabled: Boolean,
    currentScreenNumber: Int?,
    numOfScreens: Int,
    navigateBackButtonOnClick: () -> Unit,
    navigateForwardButtonOnClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            modifier = Modifier.weight(1f),
            onClick = navigateBackButtonOnClick,
        ) {
            Text(
                text = navigateBackButtonText,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        currentScreenNumber?.let {
            NavigationProgressIndicator(
                currentScreenNumber = it,
                numOfScreens = numOfScreens
            )
        }

        TextButton(
            modifier = Modifier.weight(1f),
            onClick = navigateForwardButtonOnClick,
            enabled = navigateForwardButtonIsEnabled,
        ) {
            Text(text = navigateForwardButtonText)
        }
    }
}

@Composable
private fun NavigationProgressIndicator(
    modifier: Modifier = Modifier,
    currentScreenNumber: Int,
    numOfScreens: Int,
) {
    Row(modifier) {
        repeat(numOfScreens) { screenNumber ->
            val filledBackground = (screenNumber + 1) <= currentScreenNumber
            Box(
                modifier = Modifier
                    .padding(1.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    )
                    .background(
                        color = if (filledBackground) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
            )
        }
    }
}

@Composable
fun AddHabitDestinationTopAppBar(
    modifier: Modifier = Modifier,
    destination: AddRoutineDestination,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(112.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.BottomStart),
            text = stringResource(id = destination.screenTitleId),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AddRoutineBottomNavigationPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        AddRoutineBottomNavigation(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            navigateBackButtonText = "BACK",
            navigateForwardButtonText = "NEXT",
            navigateForwardButtonIsEnabled = true,
            currentScreenNumber = 3,
            numOfScreens = 4,
            navigateBackButtonOnClick = { },
            navigateForwardButtonOnClick = { },
        )
    }
}