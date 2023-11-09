package com.rendox.routinetracker.core.ui.helpers

sealed interface UiState {
    object Loading : UiState
    data class Success<T>(val value: T) : UiState
}