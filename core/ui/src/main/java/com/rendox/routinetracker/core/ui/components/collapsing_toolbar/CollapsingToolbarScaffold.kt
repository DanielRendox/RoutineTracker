package com.rendox.routinetracker.core.ui.components.collapsing_toolbar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.scroll_behavior.ToolbarState
import com.rendox.routinetracker.core.ui.components.collapsing_toolbar.scroll_behavior.rememberExitUntilCollapsedToolbarState

@Composable
fun CollapsingToolbarScaffold(
    modifier: Modifier = Modifier,
    toolbarHeightRange: IntRange,
    scrollState: ScrollState = rememberScrollState(),
    toolbarState: ToolbarState,
    toolbar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    toolbarState.scrollValue = scrollState.value

    Surface(modifier = modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = contentWindowInsets.exclude(WindowInsets.statusBars),
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            contentColor = contentColor,
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Box(modifier = Modifier.statusBarsPadding()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                    ) {
                        val toolbarExpandedHeight = with(LocalDensity.current) {
                            toolbarHeightRange.last.toDp()
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(toolbarExpandedHeight)
                        )
                        content()
                    }
                }
                toolbar()
            }
        }
    }
}

@Preview
@Composable
fun CollapsingToolbarScaffoldPreview() {
    val toolbarHeightRange = with(LocalDensity.current) {
        LargeToolbarHeightCollapsed.roundToPx()..LargeToolbarHeightExpanded.roundToPx()
    }
    val toolbarState = rememberExitUntilCollapsedToolbarState(toolbarHeightRange)

    CollapsingToolbarScaffold(
        toolbarHeightRange = toolbarHeightRange,
        toolbarState = toolbarState,
        toolbar = {
            CollapsingToolbarLarge(
                modifier = Modifier.fillMaxWidth(),
                title = "Become an awesome developer",
                toolbarState = toolbarState,
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
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
        },
    ) {
        Text(
            modifier = Modifier.padding(
                top = LargeToolbarBottomPadding,
                start = 16.dp,
                end = 16.dp
            ),
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl. Donec euismod, nisl eget aliquam aliquam, nisl nunc aliquet nunc, quis aliquam nisl nisl quis nisl.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}