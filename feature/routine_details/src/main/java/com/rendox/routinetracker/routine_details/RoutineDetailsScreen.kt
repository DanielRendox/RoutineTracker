package com.rendox.routinetracker.routine_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.rendox.routinetracker.routine_details.navigation.RoutineDetailsNavHost
import com.rendox.routinetracker.routine_details.navigation.calendarNavRoute

@Composable
internal fun RoutineDetailsRoute(
    modifier: Modifier = Modifier,
    routineId: Long,
    navigateToPreviousScreen: () -> Unit,
) {
    RoutineDetailsScreen(modifier, routineId, navigateToPreviousScreen)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineDetailsScreen(
    modifier: Modifier = Modifier,
    routineId: Long,
    navigateToPreviousScreen: () -> Unit,
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
                        "Routine name",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateToPreviousScreen() }) {
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