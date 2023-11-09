package com.rendox.routinetracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomTextColors(
    val negative: Color,
    val positive: Color,
)

val customTextColorsDark = CustomTextColors(
    negative = red_negative_text_dark,
    positive = green_positive_text_dark,
)

val customTextColorsLight = CustomTextColors(
    negative = red_negative_text_light,
    positive = green_positive_text_light,
)

val LocalCustomTextColors = compositionLocalOf { customTextColorsLight }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.customTextColors: CustomTextColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomTextColors.current