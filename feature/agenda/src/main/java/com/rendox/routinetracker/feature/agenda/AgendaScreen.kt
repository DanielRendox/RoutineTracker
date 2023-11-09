package com.rendox.routinetracker.feature.agenda

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.feature.agenda.databinding.AgendaRecyclerviewBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    modifier: Modifier = Modifier,
    routineNamesList: List<String>,
    statusList: List<RoutineStatus>,
    completionTimeList: List<LocalTime?>,
) {
    val locale = LocalLocale.current
    val today = LocalDate.now()
    var currentDate by remember { mutableStateOf(today) }

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
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ }
            ) {
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
                dateOnClick = {
                    currentDate = it
                },
                today = today,
            )
            AgendaList(
                routineNamesList, statusList, completionTimeList
            )
        }
    }
}

@Composable
private fun AgendaList(
    routineNamesList: List<String>,
    statusList: List<RoutineStatus>,
    completionTimeList: List<LocalTime?>,
) {
    AndroidViewBinding(AgendaRecyclerviewBinding::inflate) {
        val adapter = AgendaListAdapter(
            routineNamesList, statusList, completionTimeList
        )
        agendaRecyclerview.adapter = adapter
    }
}

