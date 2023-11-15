package com.rendox.routinetracker.feature.agenda

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.feature.agenda.databinding.AgendaRecyclerviewBinding
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

@Composable
internal fun AgendaRoute(
    modifier: Modifier = Modifier,
    onRoutineClick: (Long) -> Unit,
    onAddRoutineClick: () -> Unit,
    viewModel: AgendaScreenViewModel = koinViewModel(),
) {
    val currentDate by viewModel.currentDateFlow.collectAsStateWithLifecycle()
    val visibleRoutines by viewModel.visibleRoutinesFlow.collectAsStateWithLifecycle()

    AgendaScreen(
        modifier = modifier,
        currentDate = currentDate.toJavaLocalDate(),
        routineList = visibleRoutines,
        today = LocalDate.now(),
        onAddRoutineClick = { viewModel.onAddRoutineClick() },
        onRoutineClick = onRoutineClick,
        onStatusCheckmarkClick = { routineId, status ->
            viewModel.onRoutineStatusCheckmarkClick(routineId, currentDate, status)
        },
        onDateChange = { viewModel.onDateChange(it.toKotlinLocalDate()) },
        onNotDueRoutinesVisibilityToggle = {
            viewModel.onNotDueRoutinesVisibilityToggle()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgendaScreen(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    routineList: List<RoutineListElement>,
    today: LocalDate,
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Long) -> Unit,
    onStatusCheckmarkClick: (Long, RoutineStatus) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNotDueRoutinesVisibilityToggle: () -> Unit,
) {
    val locale = LocalLocale.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val dateFormatter =
                remember { DateTimeFormatter.ofPattern("d MMM yyyy", locale) }
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(text = currentDate.format(dateFormatter))
                },
                actions = {
                    Row {
                        IconButton(onClick = onNotDueRoutinesVisibilityToggle) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_visibility_24),
                                contentDescription = "Hide not due routines"
                            )
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
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
        ) {
            RoutineTrackerWeekCalendar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp)
                    .height(70.dp),
                selectedDate = currentDate,
                initialDate = today,
                firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek,
                dateOnClick = onDateChange,
                today = today,
            )
            AgendaList(
                routineList = routineList,
                onRoutineClick = onRoutineClick,
                onStatusCheckmarkClick = { routineId, status ->
                    onStatusCheckmarkClick(routineId, status)
                },
            )
        }
    }
}

@Composable
private fun AgendaList(
    routineList: List<RoutineListElement>,
    onRoutineClick: (Long) -> Unit,
    onStatusCheckmarkClick: (Long, RoutineStatus) -> Unit,
) {
    AndroidViewBinding(AgendaRecyclerviewBinding::inflate) {
        val adapter = AgendaListAdapter(
            routineList = routineList,
            onRoutineClick = onRoutineClick,
            onStatusCheckmarkClick = onStatusCheckmarkClick,
        )
        agendaRecyclerview.adapter = adapter
    }
}

