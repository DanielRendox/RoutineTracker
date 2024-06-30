package com.rendox.routinetracker.routine_details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.CollapsingToolbarLarge
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.CollapsingToolbarScaffold
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.LargeToolbarHeightCollapsed
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.LargeToolbarHeightExpanded
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.scroll_behavior.rememberExitUntilCollapsedToolbarState
import com.rendox.routinetracker.core.ui.helpers.ObserveUiEvent
import com.rendox.routinetracker.feature.routine_details.R
import com.rendox.routinetracker.routine_details.calendar.RoutineCalendarScreen
import java.time.YearMonth
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun RoutineDetailsRoute(
    modifier: Modifier = Modifier,
    routineId: Long,
    viewModel: RoutineDetailsScreenViewModel = koinViewModel(parameters = { parametersOf(routineId) }),
    navigateBack: () -> Unit,
) {
    val habit by viewModel.habitFlow.collectAsStateWithLifecycle()
    val routineCalendarDates by viewModel.calendarDatesFlow.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonthFlow.collectAsStateWithLifecycle()
    val currentStreakDurationInDays by viewModel.currentStreakDurationInDays.collectAsStateWithLifecycle()
    val longestStreakDurationInDays by viewModel.longestStreakDurationInDays.collectAsStateWithLifecycle()
    val completionAttemptBlockedEvent by viewModel.completionAttemptBlockedEvent.collectAsStateWithLifecycle()
    val navigateBackEvent by viewModel.navigateBackEvent.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = when (completionAttemptBlockedEvent?.data) {
        is IllegalDateEditAttemptException.NotStartedHabitDateEditAttemptException -> {
            stringResource(
                id = com.rendox.routinetracker.core.ui.R.string.not_started_date_completion_attempt_snackbar_message,
            )
        }

        is IllegalDateEditAttemptException.FinishedHabitDateEditAttemptException -> {
            stringResource(
                id = com.rendox.routinetracker.core.ui.R.string.finished_date_completion_attempt_snackbar_message,
            )
        }

        is IllegalDateEditAttemptException.FutureDateEditAttemptException -> {
            stringResource(
                id = com.rendox.routinetracker.core.ui.R.string.future_date_completion_attempt_snackbar_message,
            )
        }

        null -> ""
    }

    ObserveUiEvent(completionAttemptBlockedEvent) {
        snackbarHostState.showSnackbar(message = snackbarMessage)
    }

    ObserveUiEvent(navigateBackEvent) {
        navigateBack()
    }

    RoutineDetailsScreen(
        modifier = modifier,
        navigateBack = navigateBack,
        snackbarHostState = snackbarHostState,
        habit = habit,
        routineCalendarDates = routineCalendarDates,
        currentMonth = currentMonth,
        currentStreakDurationInDays = currentStreakDurationInDays,
        longestStreakDurationInDays = longestStreakDurationInDays,
        onDeleteHabit = viewModel::onDeleteHabit,
        onScrolledToNewMonth = viewModel::onScrolledToNewMonth,
        insertCompletion = viewModel::onHabitComplete,
    )

    BackHandler {
        navigateBack()
    }
}

@Composable
private fun DeleteHabitDialog(
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
                    text = stringResource(id = R.string.delete_habit_dialog_title),
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
                        text = stringResource(
                            id = com.rendox.routinetracker.core.ui.R.string.delete,
                        ).replaceFirstChar { it.titlecase() },
                    )
                }
            },
        )
    }
}

@Composable
internal fun RoutineDetailsScreen(
    modifier: Modifier = Modifier,
    habit: Habit?,
    navigateBack: () -> Unit,
    onDeleteHabit: () -> Unit,
    snackbarHostState: SnackbarHostState,
    routineCalendarDates: Map<LocalDate, CalendarDateData>,
    currentMonth: YearMonth,
    currentStreakDurationInDays: Int,
    longestStreakDurationInDays: Int,
    insertCompletion: (Habit.CompletionRecord) -> Unit,
    onScrolledToNewMonth: (YearMonth) -> Unit,
) {
    val iconDescription: String = stringResource(
        id = com.rendox.routinetracker.core.ui.R.string.back,
    )

    val toolbarHeightRange = with(LocalDensity.current) {
        LargeToolbarHeightCollapsed.roundToPx()..LargeToolbarHeightExpanded.roundToPx()
    }
    val toolbarState = rememberExitUntilCollapsedToolbarState(toolbarHeightRange)

    var deleteHabitDialogIsShown by rememberSaveable { mutableStateOf(false) }
    DeleteHabitDialog(
        dialogIsVisible = deleteHabitDialogIsShown,
        onDismissRequest = { deleteHabitDialogIsShown = false },
        onConfirmButtonClicked = {
            onDeleteHabit()
            deleteHabitDialogIsShown = false
        },
    )

    CollapsingToolbarScaffold(
        modifier = modifier.fillMaxSize(),
        toolbar = {
            CollapsingToolbarLarge(
                title = habit?.name ?: "",
                toolbarState = toolbarState,
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = iconDescription,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { deleteHabitDialogIsShown = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(
                                id = R.string.delete_habit_icon_description,
                            ),
                        )
                    }
                },
            )
        },
        toolbarHeightRange = toolbarHeightRange,
        toolbarState = toolbarState,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) {
        if (habit != null) {
            RoutineCalendarScreen(
                habit = habit,
                routineCalendarDates = routineCalendarDates,
                currentMonth = currentMonth,
                currentStreakDurationInDays = currentStreakDurationInDays,
                longestStreakDurationInDays = longestStreakDurationInDays,
                insertCompletion = insertCompletion,
                onScrolledToNewMonth = onScrolledToNewMonth,
            )
        }
    }
}