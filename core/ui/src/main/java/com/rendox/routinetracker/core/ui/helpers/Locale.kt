package com.rendox.routinetracker.core.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.util.Locale

val LocalLocale = compositionLocalOf { Locale.getDefault() }

@Composable
@ReadOnlyComposable
fun getLocale(): Locale {
    val configuration = LocalConfiguration.current
    ConfigurationCompat.getLocales(configuration).get(0)?.let { return it }
    return Locale.getDefault()
}