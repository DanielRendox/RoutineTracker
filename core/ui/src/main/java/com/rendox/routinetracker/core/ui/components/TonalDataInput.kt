package com.rendox.routinetracker.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TonalDataInput(
    modifier: Modifier = Modifier,
    text: String,
) {
    Surface(
        modifier = modifier
            .widthIn(min = 96.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(30),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = text,
                style = MaterialTheme.typography.labelMedium,
            )
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