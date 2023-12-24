package com.rendox.routinetracker.add_routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rendox.routinetracker.add_routine.choose_routine_type.rememberChooseRoutineTypePageState
import com.rendox.routinetracker.add_routine.choose_schedule.rememberChooseSchedulePageState
import com.rendox.routinetracker.add_routine.navigation.AddRoutineDestination
import com.rendox.routinetracker.add_routine.navigation.AddRoutineNavHost
import com.rendox.routinetracker.add_routine.set_goal.rememberSetGoalPageState
import com.rendox.routinetracker.add_routine.tweak_routine.rememberTweakRoutinePageState
import com.rendox.routinetracker.core.data.habit.HabitRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.compose.koinInject

@OptIn(DelicateCoroutinesApi::class)
@Composable
internal fun AddRoutineRoute(
    modifier: Modifier = Modifier,
    navigateBackAndRecreate: () -> Unit,
    navigateBack: () -> Unit,
    habitRepository: HabitRepository = koinInject()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val addRoutineScreenState = rememberAddRoutineScreenState(
        navController = navController,
        navBackStackEntry = navBackStackEntry,
        chooseRoutineTypePageState = rememberChooseRoutineTypePageState(),
        setGoalPageState = rememberSetGoalPageState(),
        chooseSchedulePageState = rememberChooseSchedulePageState(),
        tweakRoutinePageState = rememberTweakRoutinePageState(),
        navigateBackAndRecreate = navigateBackAndRecreate,
        navigateBack = navigateBack,
        saveRoutine = { routine ->
            GlobalScope.launch {
                withTimeout(10_000L) {
                    println("resulting routine = $routine")
                    habitRepository.insertHabit(routine)
                }
            }
        },
    )

    AddRoutineScreen(
        modifier = modifier,
        addRoutineScreenState = addRoutineScreenState,
    )
}

@Composable
internal fun AddRoutineScreen(
    modifier: Modifier = Modifier,
    addRoutineScreenState: AddRoutineScreenState,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
            AddRoutineNavHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                navController = addRoutineScreenState.navController,
                addRoutineScreenState = addRoutineScreenState,
            )

            val navigateBackButtonText =
                addRoutineScreenState.navigateBackButtonText.asString().uppercase()

            val navigateForwardButtonText =
                addRoutineScreenState.navigateForwardButtonText.asString().uppercase()

            AddRoutineBottomNavigation(
                navigateBackButtonText = navigateBackButtonText,
                navigateForwardButtonText = navigateForwardButtonText,
                navigateForwardButtonIsEnabled = !addRoutineScreenState.containsError,
                currentScreenNumber = addRoutineScreenState.currentScreenNumber,
                numOfScreens = addRoutineScreenState.navDestinations.size,
                navigateBackButtonOnClick = addRoutineScreenState::navigateBackOrCancel,
                navigateForwardButtonOnClick = addRoutineScreenState::navigateForwardOrSave,
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
fun AddRoutineDestinationTopAppBar(
    modifier: Modifier = Modifier,
    destination: AddRoutineDestination,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp)
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