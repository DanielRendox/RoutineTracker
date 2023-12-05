package com.rendox.routinetracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomIconSetting(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable ((Modifier) -> Unit)? = null,
    trailingComponent: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let { it(Modifier.padding(end = 16.dp)) }
                Text(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp),
                    text = title
                )
            }
            trailingComponent?.let { it() }
        }
    }
}

@Preview
@Composable
private fun CustomIconSettingPreview() {
    Surface {
        CustomIconSetting(
            modifier = Modifier.width(350.dp),
            title = "Enhanced security",
            icon = {
                Icon(
                    modifier = it,
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                )
            },
            trailingComponent = {
                Switch(checked = true, onCheckedChange = {})
            },
        )
    }
}