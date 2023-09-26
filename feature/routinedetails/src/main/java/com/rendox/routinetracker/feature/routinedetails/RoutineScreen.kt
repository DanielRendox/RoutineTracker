package com.rendox.routinetracker.feature.routinedetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoutineDetailsScreen(
    modifier: Modifier,
    routineScreenState: RoutineScreenState,
) {
    Scaffold(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues = it),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    text = routineScreenState.routineName ?: "Routine name not found :(",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Row {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = routineScreenState.routineStartDate
                            ?: "Routine start date not found :("
                    )
                }
            }
        }
    }
}