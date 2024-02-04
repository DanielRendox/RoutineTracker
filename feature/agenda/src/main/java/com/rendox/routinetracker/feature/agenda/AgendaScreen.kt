package com.rendox.routinetracker.feature.agenda

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.core.domain.completion_history.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.core.ui.helpers.ObserveUiEvent
import com.rendox.routinetracker.feature.agenda.databinding.AgendaRecyclerviewBinding
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun AgendaRoute(
    modifier: Modifier = Modifier,
    onRoutineClick: (Long) -> Unit,
    onAddRoutineClick: () -> Unit,
    viewModel: AgendaScreenViewModel = koinViewModel(),
) {
    val currentDate by viewModel.currentDateFlow.collectAsStateWithLifecycle()
    val visibleRoutines by viewModel.visibleRoutinesFlow.collectAsStateWithLifecycle()
    val showAllRoutines by viewModel.showAllRoutinesFlow.collectAsStateWithLifecycle()
    val nothingIsScheduled by viewModel.nothingIsScheduledFlow.collectAsStateWithLifecycle()
    val completionAttemptBlockedEvent by viewModel.completionAttemptBlockedEvent.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = when (completionAttemptBlockedEvent?.data) {
        is IllegalDateEditAttemptException.NotStartedHabitDateEditAttemptException -> {
            stringResource(id = com.rendox.routinetracker.core.ui.R.string.not_started_date_completion_attempt_snackbar_message)
        }

        is IllegalDateEditAttemptException.FinishedHabitDateEditAttemptException -> {
            stringResource(id = com.rendox.routinetracker.core.ui.R.string.finished_date_completion_attempt_snackbar_message)
        }

        is IllegalDateEditAttemptException.FutureDateEditAttemptException -> {
            stringResource(id = com.rendox.routinetracker.core.ui.R.string.future_date_completion_attempt_snackbar_message)
        }

        null -> ""
    }

    ObserveUiEvent(completionAttemptBlockedEvent) {
        snackbarHostState.showSnackbar(message = snackbarMessage)
    }

    AgendaScreen(
        modifier = modifier,
        currentDate = currentDate.toJavaLocalDate(),
        routineList = visibleRoutines,
        today = LocalDate.now(),
        onAddRoutineClick = onAddRoutineClick,
        onRoutineClick = onRoutineClick,
        insertCompletion = { routineId, completionRecord ->
            viewModel.onRoutineComplete(routineId, completionRecord)
        },
        onDateChange = { viewModel.onDateChange(it.toKotlinLocalDate()) },
        onNotDueRoutinesVisibilityToggle = {
            viewModel.onNotDueRoutinesVisibilityToggle()
        },
        showAllRoutines = showAllRoutines,
        snackbarHostState = snackbarHostState,
        nothingIsScheduled = nothingIsScheduled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgendaScreen(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    routineList: List<DisplayRoutine>,
    today: LocalDate,
    showAllRoutines: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Long) -> Unit,
    insertCompletion: (Long, Habit.CompletionRecord) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNotDueRoutinesVisibilityToggle: () -> Unit,
    nothingIsScheduled: Boolean,
) {
    val locale = LocalLocale.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            val dateFormatter =
                remember { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = if (currentDate == today) {
                            stringResource(id = com.rendox.routinetracker.core.ui.R.string.today)
                        } else {
                            currentDate.format(dateFormatter)
                        }
                    )
                },
                actions = {
                    Row {
                        IconButton(onClick = onNotDueRoutinesVisibilityToggle) {
                            if (showAllRoutines) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_visibility_on_24),
                                    contentDescription = stringResource(
                                        id = R.string.routine_visibility_icon_toggle_all_visible_description
                                    ),
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_visibility_off_24),
                                    contentDescription = stringResource(
                                        id = R.string.routine_visibility_icon_toggle_some_routines_hidden_description
                                    ),
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRoutineClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.fab_icon_description),
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
        ) {
            val weekCalendarHeight = 70.dp

            RoutineTrackerWeekCalendar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp)
                    .height(weekCalendarHeight),
                selectedDate = currentDate,
                initialDate = today,
                dateOnClick = onDateChange,
                today = today,
            )

            if (routineList.isNotEmpty()) {
                val onStatusCheckmarkClick: (DisplayRoutine) -> Unit = { routine ->
                    when (routine.type) {
                        DisplayRoutineType.YesNoHabit -> {
                            val numOfTimesCompleted =
                                if (routine.numOfTimesCompleted > 0F) 0F else 1F
                            val completion = Habit.YesNoHabit.CompletionRecord(
                                date = currentDate.toKotlinLocalDate(),
                                numOfTimesCompleted = numOfTimesCompleted,
                            )
                            insertCompletion(routine.id, completion)
                        }
                    }
                }
                AgendaList(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    routineList = routineList,
                    onRoutineClick = onRoutineClick,
                    onStatusCheckmarkClick = onStatusCheckmarkClick,
                )
            }

            if(nothingIsScheduled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val smallTopAppBarHeight = 64.dp
                    NothingScheduled(
                        modifier = Modifier.padding(
                            bottom = when (LocalConfiguration.current.orientation) {
                                Configuration.ORIENTATION_LANDSCAPE -> 0.dp
                                else -> smallTopAppBarHeight + weekCalendarHeight
                            }
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaList(
    modifier: Modifier = Modifier,
    routineList: List<DisplayRoutine>,
    onRoutineClick: (Long) -> Unit,
    onStatusCheckmarkClick: (DisplayRoutine) -> Unit,
) {
    AndroidViewBinding(
        modifier = modifier,
        factory = AgendaRecyclerviewBinding::inflate,
    ) {
        val adapter = AgendaListAdapter(
            routineList = routineList,
            onRoutineClick = onRoutineClick,
            onCheckmarkClick = onStatusCheckmarkClick,
        )
        agendaRecyclerview.adapter = adapter
    }
}

@Composable
fun NothingScheduled(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 16.dp),
            painter = painterResource(id = R.drawable.empty_calendar_24),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = stringResource(id = R.string.nothing_scheduled),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = stringResource(id = R.string.nothing_scheduled_description_try_adding_routines),
                color = MaterialTheme.colorScheme.outline,
            )
            Icon(
                modifier = Modifier.size(14.dp),
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = stringResource(id = R.string.nothing_scheduled_description_try_toggling_routine_visibility),
                color = MaterialTheme.colorScheme.outline,
            )
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(id = R.drawable.baseline_visibility_on_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun NothingScheduledPreview() {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NothingScheduled()
        }
    }
}

