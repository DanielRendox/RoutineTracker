package com.rendox.routinetracker//package com.rendox.routinetracker.ui.routine
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.paddingFromBaseline
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.rendox.routinetracker.ui.theme.PaddingMedium
//import com.rendox.routinetracker.ui.theme.PaddingSmall
//
////@Composable
////fun RoutineListScreen(
////    modifier: Modifier = Modifier
////) {
////    LazyColumn(modifier = modifier) {
////        items(routineList) {routine ->
////
////        }
////    }
////}
//
//@Composable
//fun RoutineListItem(
//    modifier: Modifier = Modifier,
//    routine: Routine,
//) {
//    val roundedCornerShapeSize = 12.dp
//    Card(
//        modifier = modifier,
//        shape = RoundedCornerShape(roundedCornerShapeSize),
//    ) {
//        Column {
//            // routine's image
//            routine.imageId?.let {
//                Image(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(150.dp)
//                        .clip(RoundedCornerShape(roundedCornerShapeSize)),
//                    painter = painterResource(it),
//                    contentScale = ContentScale.Crop,
//                    contentDescription = null,
//                )
//            }
//            Column(modifier = Modifier.padding(PaddingMedium)) {
//                // routine's title
//                Text(
//                    modifier = Modifier.paddingFromBaseline(bottom = PaddingSmall),
//                    text = routine.title,
//                    style = MaterialTheme.typography.titleLarge,
//                )
//                // routine's description
//                routine.description?.let {
//                    Text(
//                        text = it,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Preview(
//    showBackground = true,
//    widthDp = 400,
//    heightDp = 800,
//)
//@Composable
//fun RoutineListItemPreview() {
//    RoutineListItem(routine = routineList[0])
//}