package com.rendox.routinetracker.core.ui.components.collapsing_toolbar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.scroll_behavior.ToolbarState
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.scroll_behavior.rememberExitUntilCollapsedToolbarState
import kotlin.math.roundToInt

val LargeToolbarHeightExpanded = 152.dp
val LargeToolbarHeightCollapsed = 64.dp
val LargeToolbarBottomPadding = 28.dp

@Composable
fun CollapsingToolbarLarge(
    modifier: Modifier = Modifier,
    title: String,
    toolbarState: ToolbarState,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val toolbarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
        elevation = lerp(
            start = 3.dp,
            stop = 0.dp,
            fraction = toolbarState.progress,
        )
    )

    val dynamicToolbarHeight = with(LocalDensity.current) {
        toolbarState.height.toDp()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = toolbarColor)
            }
            .statusBarsPadding()
            .height(dynamicToolbarHeight)
    ) {
        val textCompressionRatio = 0.7857F
        CollapsingTextLayout(
            progress = toolbarState.progress,
            textCompressionRatio = textCompressionRatio,
        ) {

            val textSizeWithoutCompression = 1F
            val textSize: Float = lerp(
                start = textCompressionRatio,
                stop = textSizeWithoutCompression,
                fraction = toolbarState.progress,
            )

            val textDefaultEndPadding = 16.dp
            val textCompressedEndPadding = when {
                actions == null -> 0.dp
                navigationIcon == null -> 30.dp
                else -> 100.dp
            }
            val textEndPadding: Dp = lerp(
                start = textCompressedEndPadding,
                stop = textDefaultEndPadding,
                fraction = toolbarState.progress,
            )

            val titleStartPadding = when (LocalConfiguration.current.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> 24.dp
                else -> 16.dp
            }

            val textStartPadding = if (navigationIcon == null) titleStartPadding else lerp(
                start = 0.dp,
                stop = titleStartPadding,
                fraction = toolbarState.progress,
            )

            Text(
                modifier = Modifier
                    .padding(
                        end = textEndPadding,
                        start = textStartPadding,
                    )
                    .graphicsLayer(
                        scaleX = textSize,
                        scaleY = textSize,
                        transformOrigin = TransformOrigin(0F, 0F),
                    ),
                text = title,
                maxLines = if (toolbarState.progress > 0.1F) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineMedium.copy(
                    textMotion = TextMotion.Animated
                ),
            )

            if (navigationIcon != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(LargeToolbarHeightCollapsed),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    navigationIcon()
                }
            } else {
                Spacer(modifier = Modifier)
            }

            if (actions != null){
                Box(
                    modifier = Modifier
                        .height(LargeToolbarHeightCollapsed)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Row(content = actions)
                }
            } else {
                Spacer(modifier = Modifier)
            }
        }
    }
}

@Composable
fun CollapsingTextLayout(
    modifier: Modifier = Modifier,
    progress: Float,
    textCompressionRatio: Float,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints)
        }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            val title = placeables[0]
            val navigationIcon = placeables[1]
            val actions = placeables[2]

            val compressedTextHeight: Float = title.height * textCompressionRatio
            val centeredTitlePosition =
                ((constraints.maxHeight - compressedTextHeight) * 0.5F).roundToInt()
            val bottomTitlePosition = constraints.maxHeight - title.height
            title.placeRelative(
                x = lerp(
                    start = navigationIcon.width,
                    stop = 0,
                    fraction = progress,
                ),
                y = lerp(
                    start = centeredTitlePosition,
                    stop = bottomTitlePosition,
                    fraction = progress,
                ),
            )

            navigationIcon.placeRelative(
                x = 0,
                y = 0,
            )

            actions.placeRelative(
                x = 0,
                y = 0,
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun CollapsibleTopAppBarPreview() {
    val scrollState = rememberScrollState()
    val toolbarHeightRange = with(LocalDensity.current) {
        LargeToolbarHeightCollapsed.roundToPx()..LargeToolbarHeightExpanded.roundToPx()
    }
    val toolbarState = rememberExitUntilCollapsedToolbarState(toolbarHeightRange)
    toolbarState.scrollValue = scrollState.value

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PaddingValues(top = LargeToolbarHeightExpanded).calculateTopPadding())
            )
            repeat(50) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text(
                        text = "Hello world",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
    CollapsingToolbarLarge(
        modifier = Modifier.fillMaxWidth(),
        title = "Clean up the house",
        toolbarState = toolbarState,
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            val icons = listOf(
                Icons.Default.Create,
                Icons.Default.MoreVert,
            )
            for (icon in icons) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
            }
        }
    )
}