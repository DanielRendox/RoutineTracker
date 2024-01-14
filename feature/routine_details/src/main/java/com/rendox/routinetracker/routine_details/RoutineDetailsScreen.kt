package com.rendox.routinetracker.routine_details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.core.domain.di.GetHabitUseCase
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.routine_details.navigation.RoutineDetailsNavHost
import com.rendox.routinetracker.routine_details.navigation.calendarNavRoute
import org.koin.compose.koinInject

@Composable
internal fun RoutineDetailsRoute(
    modifier: Modifier = Modifier,
    routineId: Long,
    getHabit: GetHabitUseCase = koinInject(),
    popBackStack: () -> Unit,
) {
    var routine by remember { mutableStateOf<Habit?>(null) }
    LaunchedEffect(Unit) {
        routine = getHabit(routineId)
    }

    RoutineDetailsScreen(
        modifier = modifier,
        routineId = routineId,
        routineName = routine?.name,
        popBackStack = popBackStack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineDetailsScreen(
    modifier: Modifier = Modifier,
    routineId: Long,
    routineName: String?,
    popBackStack: () -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val iconDescription: String = stringResource(
        id = com.rendox.routinetracker.core.ui.R.string.top_app_bar_navigation_icon_description
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = routineName ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = iconDescription,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            RoutineDetailsNavHost(
                startDestination = calendarNavRoute,
                routineId = routineId,
            )
        }
    }
}