package com.rendox.routinetracker.feature.agenda

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.RecyclerView
import com.rendox.routinetracker.core.model.HistoricalStatus
import com.rendox.routinetracker.core.model.PlanningStatus
import com.rendox.routinetracker.core.model.RoutineStatus
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.core.ui.theme.RoutineTrackerTheme
import com.rendox.routinetracker.core.ui.theme.routineStatusColors
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AgendaListAdapter(
    private val routineList: List<RoutineListElement>,
    private val onRoutineClick: (Long) -> Unit,
    private val onStatusCheckmarkClick: (Long, RoutineStatus) -> Unit,
) : RecyclerView.Adapter<AgendaListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendaListViewHolder {
        return AgendaListViewHolder(ComposeView(parent.context))
    }

    override fun getItemCount(): Int {
        return routineList.size
    }

    override fun onBindViewHolder(holder: AgendaListViewHolder, position: Int) {
        val routine = routineList[position]
        holder.bind(
            routine = routine,
            onRoutineClick = { onRoutineClick(routine.id) },
            onStatusCheckmarkClick = { status -> onStatusCheckmarkClick(routine.id, status) },
        )
    }
}

class AgendaListViewHolder(
    private val composeView: ComposeView
) : RecyclerView.ViewHolder(composeView) {
    fun bind(
        routine: RoutineListElement,
        onRoutineClick: () -> Unit,
        onStatusCheckmarkClick: (RoutineStatus) -> Unit,
    ) {
        composeView.setContent {
            RoutineTrackerTheme {
                AgendaItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                    routine = routine,
                    onRoutineClick = onRoutineClick,
                    onStatusCheckmarkClick = onStatusCheckmarkClick,
                )
            }
        }
    }
}

@Composable
fun AgendaItem(
    modifier: Modifier = Modifier,
    routine: RoutineListElement,
    onRoutineClick: () -> Unit,
    onStatusCheckmarkClick: (RoutineStatus) -> Unit,
) {
    val locale = LocalLocale.current
    val timeFormatter =
        remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale) }

    val notDueStatuses = remember {
        listOf(
            PlanningStatus.AlreadyCompleted,
            PlanningStatus.NotDue,
            PlanningStatus.OnVacation,
            HistoricalStatus.Skipped,
            HistoricalStatus.NotCompletedOnVacation,
            HistoricalStatus.CompletedLater,
            HistoricalStatus.AlreadyCompleted,
        )
    }

    Row(
        modifier = modifier.alpha(
            if (notDueStatuses.contains(routine.status)) 0.5f else 1f
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (routine.completionTime != null) {
            Text(
                modifier = Modifier
                    .width(100.dp)
                    .padding(end = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                text = routine.completionTime.toJavaLocalTime().format(timeFormatter),
            )
        } else {
            Spacer(modifier = Modifier.width(100.dp))
        }

        StatusCheckmark(
            modifier = Modifier
                .padding(end = 15.dp),
            status = routine.status,
            onClick = onStatusCheckmarkClick,
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { onRoutineClick() }) {
            Column {
                Text(
                    modifier = Modifier.padding(bottom = 2.dp),
                    text = routine.name,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .paddingFromBaseline(2.dp),
                        text = routine.status.toString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCheckmark(
    modifier: Modifier = Modifier,
    status: RoutineStatus,
    onClick: (RoutineStatus) -> Unit,
) {
    val backgroundColor: Color
    val icon: ImageVector?
    val iconColor: Color?

    when (status) {
        HistoricalStatus.NotCompleted -> {
            backgroundColor = MaterialTheme.routineStatusColors.failedBackgroundLight
            icon = Icons.Filled.Close
            iconColor = MaterialTheme.routineStatusColors.failedStroke
        }

        PlanningStatus.Planned -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        PlanningStatus.Backlog -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        PlanningStatus.AlreadyCompleted -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        PlanningStatus.NotDue -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        PlanningStatus.OnVacation -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        HistoricalStatus.Skipped -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        HistoricalStatus.NotCompletedOnVacation -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        HistoricalStatus.CompletedLater -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        HistoricalStatus.AlreadyCompleted -> {
            backgroundColor = MaterialTheme.routineStatusColors.pending
            icon = null
            iconColor = null
        }

        HistoricalStatus.Completed -> {
            backgroundColor = MaterialTheme.routineStatusColors.completedBackgroundLight
            icon = Icons.Filled.Done
            iconColor = MaterialTheme.routineStatusColors.completedStroke
        }

        HistoricalStatus.OverCompleted -> {
            backgroundColor = MaterialTheme.routineStatusColors.completedBackgroundLight
            icon = Icons.Filled.Done
            iconColor = MaterialTheme.routineStatusColors.completedStroke
        }

        HistoricalStatus.OverCompletedOnVacation -> {
            backgroundColor = MaterialTheme.routineStatusColors.completedBackgroundLight
            icon = Icons.Filled.Done
            iconColor = MaterialTheme.routineStatusColors.completedStroke
        }

        HistoricalStatus.SortedOutBacklogOnVacation -> {
            backgroundColor = MaterialTheme.routineStatusColors.completedBackgroundLight
            icon = Icons.Filled.Done
            iconColor = MaterialTheme.routineStatusColors.completedStroke
        }

        HistoricalStatus.SortedOutBacklog -> {
            backgroundColor = MaterialTheme.routineStatusColors.completedBackgroundLight
            icon = Icons.Filled.Done
            iconColor = MaterialTheme.routineStatusColors.completedStroke
        }
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick(status) }
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                imageVector = it,
                contentDescription = null,
                tint = iconColor!!,
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AgendaItemInListPreview() {

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            for (routine in routines) {
                AgendaItem(
                    modifier = Modifier.padding(vertical = 12.dp),
                    routine = routine,
                    onRoutineClick = {},
                    onStatusCheckmarkClick = {},
                )
            }
        }
    }
}

private val routines = listOf(
    RoutineListElement(
        name = "Do sports",
        status = HistoricalStatus.Completed,
        completionTime = kotlinx.datetime.LocalTime(hour = 9, minute = 0),
        id = 1,
    ),
    RoutineListElement(
        name = "Learn new English words",
        status = PlanningStatus.Planned,
        completionTime = null,
        id = 2,
    ),
    RoutineListElement(
        name = "Spend time outside",
        status = HistoricalStatus.NotCompleted,
        completionTime = kotlinx.datetime.LocalTime(hour = 12, minute = 30),
        id = 3,
    ),
    RoutineListElement(
        name = "Make my app",
        status = HistoricalStatus.CompletedLater,
        completionTime = kotlinx.datetime.LocalTime(hour = 17, minute = 0),
        id = 4,
    )
)