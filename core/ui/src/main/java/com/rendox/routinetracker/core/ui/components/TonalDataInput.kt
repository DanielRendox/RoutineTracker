package com.rendox.routinetracker.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.core.ui.R

const val TonalDataInputRoundedCornerPercent = 30

@Composable
fun TonalDataInput(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {},
    disabled: Boolean = true,
) {
    Surface(
        modifier = modifier
            .widthIn(min = 96.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(TonalDataInputRoundedCornerPercent),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(
            modifier = Modifier.then(
                if (disabled) Modifier else Modifier.clickable(onClick = onClick),
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = text,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun TonalDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit,
    options: List<String>,
    onDropdownMenuClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(TonalDataInputRoundedCornerPercent))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(
                onClick = onDropdownMenuClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ),
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                )
                .widthIn(min = 120.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = options[selectedOptionIndex],
                style = MaterialTheme.typography.labelMedium,
            )

            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                label = "animateFloatAsState",
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            )

            Icon(
                modifier = Modifier.graphicsLayer {
                    rotationZ = iconRotation
                },
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.toggle_dropdown_menu_visibility),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                        )
                    },
                    onClick = {
                        onOptionSelected(index)
                        onDismissRequest()
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun ButtonLikeDataInputPreview() {
    Surface {
        TonalDataInput(modifier = Modifier.padding(8.dp), text = "02/12/2023")
    }
}