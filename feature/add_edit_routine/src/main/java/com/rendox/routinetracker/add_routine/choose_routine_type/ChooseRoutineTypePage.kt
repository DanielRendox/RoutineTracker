package com.rendox.routinetracker.add_routine.choose_routine_type

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.add_routine.AddRoutineDestinationTopAppBar
import com.rendox.routinetracker.add_routine.navigation.AddRoutineDestination
import com.rendox.routinetracker.feature.agenda.R

@Composable
fun ChooseRoutineTypePage(
    modifier: Modifier = Modifier,
    chooseRoutineTypePageState: ChooseRoutineTypePageState,
    navigateForward: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AddRoutineDestinationTopAppBar(
            destination = AddRoutineDestination.ChooseRoutineType
        )

        for (routineType in routineTypes) {
            ChooseRoutineTypeButton(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                routineType = routineType,
                onClick = {
                    chooseRoutineTypePageState.updateRoutineType(routineType)
                    navigateForward()
                },
            )
        }
    }
}

@Composable
private fun ChooseRoutineTypeButton(
    modifier: Modifier = Modifier,
    routineType: RoutineTypeUi,
    onClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            onClick = onClick,
            enabled = !routineType.inDevelopment,
        ) {
            Text(
                text = stringResource(id = routineType.titleId),
                style = MaterialTheme.typography.titleSmall,
            )
        }

        Text(
            modifier = Modifier.padding(top = 2.dp).fillMaxWidth(),
            text = stringResource(id = routineType.descriptionId),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (routineType.inDevelopment) {
            Text(
                modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                text = stringResource(id = R.string.in_development),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}