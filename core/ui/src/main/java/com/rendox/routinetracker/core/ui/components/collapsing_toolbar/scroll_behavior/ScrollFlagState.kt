package com.rendox.routinetracker.core.ui.components.collapsing_toolbar.scroll_behavior

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy

abstract class ScrollFlagState(
    heightRange: IntRange,
    scrollValue: Int
): ToolbarState {

    init {
        require(heightRange.first >= 0 && heightRange.last >= heightRange.first) {
            "The lowest height value must be >= 0 and the highest height value must be >= the lowest value."
        }
    }

    protected val minHeight = heightRange.first
    protected val maxHeight = heightRange.last
    protected val rangeDifference = heightRange.last - heightRange.first

    protected var _scrollValue by mutableStateOf(
        value = scrollValue.coerceAtLeast(0),
        policy = structuralEqualityPolicy()
    )

    final override val progress: Float
        get() = 1 - (maxHeight - height) / rangeDifference
}