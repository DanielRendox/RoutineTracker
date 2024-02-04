package com.rendox.routinetracker.feature.agenda

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.RecyclerView
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.ui.helpers.getStringResourceId
import com.rendox.routinetracker.core.ui.theme.routineStatusColors

class AgendaListAdapter(
    private val routineList: List<DisplayRoutine>,
    private val onRoutineClick: (Long) -> Unit,
    private val onCheckmarkClick: (DisplayRoutine) -> Unit,
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
            onStatusCheckmarkClick = { onCheckmarkClick(routine) },
        )
    }
}

class AgendaListViewHolder(
    private val composeView: ComposeView
) : RecyclerView.ViewHolder(composeView) {
    fun bind(
        routine: DisplayRoutine,
        onRoutineClick: () -> Unit,
        onStatusCheckmarkClick: () -> Unit,
    ) {
        composeView.setContent {
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

@Composable
fun AgendaItem(
    modifier: Modifier = Modifier,
    routine: DisplayRoutine,
    onRoutineClick: () -> Unit,
    onStatusCheckmarkClick: () -> Unit,
) {
    Row(
        modifier = modifier.alpha(
            if (routine.hasGrayedOutLook) 0.5f else 1f
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusCheckmark(
            modifier = Modifier
                .padding(end = 16.dp),
            status = routine.status,
            onClick = onStatusCheckmarkClick,
            statusToggleIsDisabled = routine.statusToggleIsDisabled,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRoutineClick,
                )
        ) {
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
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .paddingFromBaseline(2.dp),
                        text = stringResource(id = routine.status.getStringResourceId()),
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
    status: HabitStatus,
    onClick: () -> Unit,
    statusToggleIsDisabled: Boolean,
) {
    val backgroundColor: Color
    var icon: ImageVector?
    var iconColor: Color?

    when (status) {
        HabitStatus.Failed -> {
            backgroundColor = MaterialTheme.routineStatusColors.skippedOutOfStreak
            icon = Icons.Filled.Close
            iconColor = MaterialTheme.routineStatusColors.failedStroke
        }

        HabitStatus.Planned, HabitStatus.OnVacation, HabitStatus.NotDue,
        HabitStatus.Backlog, HabitStatus.PastDateAlreadyCompleted,
        HabitStatus.FutureDateAlreadyCompleted, HabitStatus.CompletedLater,
        HabitStatus.NotStarted, HabitStatus.Finished, HabitStatus.Skipped -> {
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
            icon = null
            iconColor = null
        }

        HabitStatus.Completed, HabitStatus.OverCompleted, HabitStatus.SortedOutBacklog,
        HabitStatus.PartiallyCompleted -> {
            backgroundColor = MaterialTheme.routineStatusColors.skippedInStreak
            icon = Icons.Filled.Done
            iconColor = MaterialTheme.routineStatusColors.completedStroke
        }
    }

    val iconSize: Dp
    if (statusToggleIsDisabled) {
        icon = Icons.Outlined.Lock
        iconColor = MaterialTheme.colorScheme.outline
        iconSize = 18.dp
    } else {
        iconSize = 24.dp
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (statusToggleIsDisabled) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
    ) {
        icon?.let {
            Icon(
                modifier = Modifier
                    .size(iconSize)
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
    DisplayRoutine(
        name = "Do sports",
        status = HabitStatus.Completed,
        completionTime = kotlinx.datetime.LocalTime(hour = 9, minute = 0),
        id = 1,
        hasGrayedOutLook = false,
        statusToggleIsDisabled = false,
        type = DisplayRoutineType.YesNoHabit,
        numOfTimesCompleted = 1f,
    ),
    DisplayRoutine(
        name = "Learn new English words",
        status = HabitStatus.Planned,
        completionTime = null,
        id = 2,
        hasGrayedOutLook = false,
        statusToggleIsDisabled = false,
        type = DisplayRoutineType.YesNoHabit,
        numOfTimesCompleted = 0f,
    ),
    DisplayRoutine(
        name = "Spend time outside",
        status = HabitStatus.Failed,
        completionTime = kotlinx.datetime.LocalTime(hour = 12, minute = 30),
        id = 3,
        hasGrayedOutLook = false,
        statusToggleIsDisabled = true,
        type = DisplayRoutineType.YesNoHabit,
        numOfTimesCompleted = 0f,
    ),
    DisplayRoutine(
        name = "Make my app",
        status = HabitStatus.CompletedLater,
        completionTime = kotlinx.datetime.LocalTime(hour = 17, minute = 0),
        id = 4,
        hasGrayedOutLook = true,
        statusToggleIsDisabled = false,
        type = DisplayRoutineType.YesNoHabit,
        numOfTimesCompleted = 0f,
    )
)