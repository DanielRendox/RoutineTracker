package com.rendox.routinetracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class RoutineStatusColors(
    val completedBackground: Color,
    val completedStroke: Color,
    val failedBackground: Color,
    val failedStroke: Color,
    val vacationBackground: Color,
    val vacationStroke: Color,
    val completedBackgroundLight: Color,
    val failedBackgroundLight: Color,
    val pending: Color,
)

val routineStatusColorsLight = RoutineStatusColors(
    completedBackground = routine_status_light_completed_background,
    completedStroke = routine_status_light_completed_stroke,
    failedBackground = routine_status_light_failed_background,
    failedStroke = routine_status_light_failed_stroke,
    vacationBackground = routine_status_light_vacation_background,
    vacationStroke = routine_status_light_vacation_stroke,
    pending = routine_status_light_pending,
    completedBackgroundLight = routine_status_light_skipped_in_streak,
    failedBackgroundLight = routine_status_light_skipped_out_of_streak,
)

val routineStatusColorsDark = RoutineStatusColors(
    completedBackground = routine_status_dark_completed_background,
    completedStroke = routine_status_dark_completed_stroke,
    failedBackground = routine_status_dark_failed_background,
    failedStroke = routine_status_dark_failed_stroke,
    vacationBackground = routine_status_dark_vacation_background,
    vacationStroke = routine_status_dark_vacation_stroke,
    pending = routine_status_dark_pending,
    completedBackgroundLight = routine_status_dark_skipped_in_streak,
    failedBackgroundLight = routine_status_dark_skipped_out_of_streak,
)

val LocalRoutineStatusColors = compositionLocalOf { routineStatusColorsLight }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.routineStatusColors: RoutineStatusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalRoutineStatusColors.current