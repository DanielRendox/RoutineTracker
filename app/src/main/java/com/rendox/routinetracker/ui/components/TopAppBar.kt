package com.rendox.routinetracker.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendox.routinetracker.ui.components.TopAppBarDefaults.BarHorizontalPadding
import com.rendox.routinetracker.ui.components.TopAppBarDefaults.BarMaxHeight
import com.rendox.routinetracker.ui.components.TopAppBarDefaults.BarMinHeight
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.CollapsingToolbarScope

object TopAppBarDefaults {
    val BarMinHeight = 64.dp
    val BarMaxHeight = 200.dp
    val BarHorizontalPadding = 4.dp
    val TitleDefaultSize = 20.sp
}

/**
 * A common top app bar with icons aligned in a row, the navigation icon at the start,
 * and action icons at the end.
 * Don't forget to apply width, height, background, and padding to this component.
 * @param navigationIcon the navigation icon displayed at the start of the top app bar. This should
 * typically be an [IconButton] or [IconToggleButton].
 * @param actions the actions displayed at the end of the top app bar. This should typically be
 * [IconButton]s placed in a row.
 */
@Composable
fun TopAppBarIcons(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            navigationIcon()
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            actions()
        }
    }
}

/**
 * Creates a top app bar with or without an image. The image collapses
 * with a fading effect when scrolled down. If the image is not provided,
 * the app bar behaves like a common app bar and never leaves the screen.
 * The title goes up and gets smaller when scrolled down.
 *
 * The function must be called within the [CollapsingToolbarScope] and
 * doesn't have a modifier, because CollapsingToolbarScaffold provides a
 * parameter for the modifier. This function is essentially a bunch of
 * components to be passed to the CollapsingToolbarScaffold as a toolbar.
 *
 * @param imageId drawable resource of the image
 * @param scaffoldState scaffold's state that represents scrolling progress
 * @param navigationIcon the navigation icon displayed at the start of the top app bar. This should
 * typically be an [IconButton] or [IconToggleButton].
 * @param actions the actions displayed at the end of the top app bar. This should typically be
 * [IconButton]s.
 */
@Composable
fun CollapsingToolbarScope.CollapsingTopAppBar(

    @DrawableRes imageId: Int? = null,
    scaffoldState: CollapsingToolbarScaffoldState,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},

    barMinHeight: Dp = BarMinHeight,
    barMaxHeight: Dp = BarMaxHeight,
) {

    // display image, when it's provided with the expanded height;
    // the image can be smaller though
    imageId?.let {
        Image(
            painter = painterResource(imageId),
            contentDescription = null,
            modifier = Modifier
                .heightIn(max = barMaxHeight)
                .fillMaxWidth()
                .graphicsLayer {
                    // fading effect as the scrolling progresses
                    alpha = scaffoldState.toolbarState.progress
                },
            contentScale = ContentScale.Crop,
        )
    }

    TopAppBarIcons(
        modifier = Modifier
            .fillMaxWidth()
            .height(barMinHeight)
            .padding(horizontal = BarHorizontalPadding),
        navigationIcon = navigationIcon,
        actions = actions,
    )
}