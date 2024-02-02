package com.rendox.routinetracker.core.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

interface UiEvent<T> {
    val data: T
    fun onConsumed()
}

@Composable
fun <T> ObserveUiEvent(
    event : UiEvent<T>?,
    onEvent: suspend () -> Unit,
) {
    LaunchedEffect(event) {
        if (event != null) {
            onEvent()
            event.onConsumed()
        }
    }
}