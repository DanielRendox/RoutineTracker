package com.rendox.routinetracker.add_routine.tweak_routine

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TweakRoutineScreen(
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = {},
    ) {
        Text(text = "Click me!")
    }
}