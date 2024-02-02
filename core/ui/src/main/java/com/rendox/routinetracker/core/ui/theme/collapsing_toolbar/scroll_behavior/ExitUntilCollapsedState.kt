package com.rendox.routinetracker.core.ui.theme.collapsing_toolbar.scroll_behavior

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable

class ExitUntilCollapsedState(
    heightRange: IntRange,
    scrollValue: Int = 0
) : ScrollFlagState(heightRange, scrollValue) {

    override val offset: Float = 0f

    override val height: Float
        get() = (maxHeight.toFloat() - scrollValue).coerceIn(minHeight.toFloat(), maxHeight.toFloat())

    override var scrollValue: Int
        get() = _scrollValue
        set(value) {
            _scrollValue = value.coerceAtLeast(0)
        }

    companion object {
        val Saver = run {
            val minHeightKey = "MinHeight"
            val maxHeightKey = "MaxHeight"
            val scrollValueKey = "ScrollValue"

            mapSaver(
                save = {
                    mapOf(
                        minHeightKey to it.minHeight,
                        maxHeightKey to it.maxHeight,
                        scrollValueKey to it.scrollValue
                    )
                },
                restore = {
                    ExitUntilCollapsedState(
                        heightRange = (it[minHeightKey] as Int)..(it[maxHeightKey] as Int),
                        scrollValue = it[scrollValueKey] as Int
                    )
                }
            )
        }
    }
}

@Composable
fun rememberExitUntilCollapsedToolbarState(
    toolbarHeightRange: IntRange
): ToolbarState = rememberSaveable(saver = ExitUntilCollapsedState.Saver) {
    ExitUntilCollapsedState(heightRange = toolbarHeightRange)
}

