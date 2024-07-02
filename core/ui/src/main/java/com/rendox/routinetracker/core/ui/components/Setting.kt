package com.rendox.routinetracker.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Setting(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { onToggle(!isOn) } else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .alpha(if (enabled) 1f else 0.5f),
        ) {
            Text(text = title)
            description?.let {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = isOn,
            onCheckedChange = onToggle,
            enabled = enabled,
        )
    }
}

@Preview
@Composable
private fun SettingPreview() {
    Surface {
        Setting(
            modifier = Modifier.width(400.dp),
            title = "Backlog",
            description = "If left uncompleted, the habit will continue to be displayed, " +
                "even on days that are not due.",
            isOn = true,
            onToggle = {},
        )
    }
}