package com.rendox.routinetracker.feature.routinedetails//package com.rendox.routinetracker.ui.routine
//
//import androidx.annotation.DrawableRes
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material3.Card
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Tab
//import androidx.compose.material3.TabRow
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.res.stringArrayResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.rendox.routinetracker.R
//import com.rendox.routinetracker.ui.components.CollapsingToolbarLarge
//import me.onebone.toolbar.CollapsingToolbarScaffold
//import me.onebone.toolbar.ScrollStrategy
//import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
//import kotlin.math.roundToInt
//
//@Composable
//fun RoutineItemScreen(
//    modifier: Modifier = Modifier,
//    @DrawableRes imageId: Int? = null,
//    title: String,
//    routineProgress: Float,
//    description: String,
//    amountOfWorkToday: Int,
//    amountOfWorkTodayCompleted: Int,
//) {
//    val scaffoldState = rememberCollapsingToolbarScaffoldState()
//    // TODO check this: scaffoldState.toolbarState.collapse()
//    CollapsingToolbarScaffold(
//        modifier = modifier.fillMaxSize(),
//        state = scaffoldState,
//        scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
//        toolbarModifier = Modifier.background(MaterialTheme.colorScheme.primary),
//        toolbar = {
//            CollapsingToolbarLarge(
//                imageId = imageId,
//                toolbarState = scaffoldState.toolbarState,
//                navigationIcon = { TopAppBarNavigationIcon() },
//                actions = { TopAppBarActions() },
//            )
//        }
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth(),
//            contentPadding = PaddingValues(16.dp),
//        ) {
//            item {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.headlineMedium,
//                )
//            }
//            item {
//                RoutineProgressBar(
//                    modifier = modifier.padding(top = 8.dp),
//                    routineProgress = routineProgress
//                )
//            }
//            item {
//                Text(
//                    modifier = Modifier.padding(top = 8.dp),
//                    text = description,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//            item {
//                val selectedTabIndex = 0 /* TODO */
//                TabRow(
//                    selectedTabIndex = selectedTabIndex,
//                    modifier = Modifier.padding(top = 8.dp)
//                ) {
//                    stringArrayResource(R.array.routine_item_tabs)
//                        .forEachIndexed { index, title ->
//                            Tab(
//                                text = { Text(title) },
//                                selected = selectedTabIndex == index,
//                                onClick = { /*TODO*/ })
//                        }
//                }
//            }
//            // TODO add tabs with their content
//            items(30) {
//                Text(text = "Item $it")
//            }
//        }
//    }
//}
//
//@Preview(
//    widthDp = 400,
//    heightDp = 800,
//    showBackground = true,
//    showSystemUi = true,
//)
//@Composable
//fun RoutineUpperPartPreview() {
//    RoutineItemScreen(
//        title = "Do sports",
////        imageId = R.drawable.cycling,
//        routineProgress = 0.25f,
//        description = "Stay fit and healthy Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim adz minim veniam, quis nostrud",
//        amountOfWorkToday = 4,
//        amountOfWorkTodayCompleted = 1,
//    )
//}
//
//@Composable
//fun TopAppBarActions(modifier: Modifier = Modifier) {
//    Row(modifier = modifier) {
//        repeat(2) {
//            IconButton(
//                onClick = { /*TODO*/ },
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Add,
//                    contentDescription = stringResource(R.string.back_icon_description),
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun TopAppBarNavigationIcon(modifier: Modifier = Modifier) {
//    IconButton(
//        modifier = modifier,
//        onClick = { /*TODO*/ },
//    ) {
//        Icon(
//            imageVector = Icons.Filled.Menu,
//            contentDescription = stringResource(R.string.back_icon_description),
//        )
//    }
//}
//
//@Composable
//fun RoutineProgressBar(
//    modifier: Modifier = Modifier,
//    routineProgress: Float,
//) {
//    Row(
//        modifier = modifier,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Text(
//            modifier = Modifier.padding(end = 8.dp),
//            text = (routineProgress * 100).roundToInt().toString() + "%",
//            style = MaterialTheme.typography.labelSmall,
//        )
//        LinearProgressIndicator(
//            modifier = Modifier.weight(1f),
//            progress = routineProgress,
//            strokeCap = StrokeCap.Round,
//        )
//    }
//}
//
//@Composable
//fun ScheduleDetailsRow(modifier: Modifier = Modifier) {
//    Row(
//        modifier = modifier,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//
//    }
//}
//
//@Composable
//fun ProgressDetailsCard(modifier: Modifier = Modifier) {
//    Card(
//        modifier = modifier,
//    ) {
//
//    }
//}