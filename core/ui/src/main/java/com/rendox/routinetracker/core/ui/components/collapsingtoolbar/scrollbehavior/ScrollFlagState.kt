package com.rendox.routinetracker.core.ui.components.collapsingtoolbar.scrollbehavior

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

abstract class ScrollFlagState(
    heightRange: IntRange,
    scrollValue: Int,
) : ToolbarState {

    init {
        require(heightRange.first >= 0 && heightRange.last >= heightRange.first) {
            "The lowest height value must be >= 0 and the highest height value must be >= the lowest value."
        }
    }

    protected val minHeight = heightRange.first
    protected val maxHeight = heightRange.last
    private val rangeDifference = heightRange.last - heightRange.first

    protected var scrollValueLimited by mutableIntStateOf(
        value = scrollValue.coerceAtLeast(0),
    )

    final override val progress: Float
        get() = 1 - (maxHeight - height) / rangeDifference
}