package com.rendox.routinetracker.core.ui.components.collapsing_toolbar.scroll_behavior

import androidx.compose.runtime.Stable

@Stable
interface ToolbarState {
    val offset: Float
    val height: Float
    val progress: Float
    var scrollValue: Int
}