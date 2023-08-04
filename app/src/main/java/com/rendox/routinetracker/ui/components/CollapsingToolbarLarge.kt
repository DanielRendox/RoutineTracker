package com.rendox.routinetracker.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.CollapsingToolbarState

/**
 * Creates a top app bar with or without an image. The image collapses
 * with a fading effect when scrolled down. If the image is not provided,
 * the app bar behaves like a common app bar and never leaves the screen.
 *
 * The function must be called within the [CollapsingToolbarScope] and
 * doesn't have a modifier, because CollapsingToolbarScaffold provides a
 * parameter for the modifier. This function is essentially a bunch of
 * components to be passed to the CollapsingToolbarScaffold as a toolbar.
 *
 * @param imageId drawable resource of the image
 * @param toolbarState scaffold's state that represents scrolling progress
 * @param navigationIcon the navigation icon displayed at the start of the top app bar. This should
 * typically be an [IconButton] or [IconToggleButton].
 * @param actions the actions displayed at the end of the top app bar. This should typically be
 * [IconButton]s.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbarLarge(
    @DrawableRes imageId: Int? = null,
    toolbarState: CollapsingToolbarState,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    barMaxHeight: Dp = 200.dp,
) {

    imageId?.let {
        Image(
            painter = painterResource(imageId),
            contentDescription = null,
            modifier = Modifier
                .height(barMaxHeight)
                .fillMaxWidth()
                .graphicsLayer {
                    // fading effect as the scrolling progresses
                    alpha = toolbarState.progress
                },
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
    }

    TopAppBar(
        title = {},
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
        )
    )
}