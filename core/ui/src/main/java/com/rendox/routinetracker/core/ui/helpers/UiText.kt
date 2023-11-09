package com.rendox.routinetracker.core.ui.helpers

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText
    class PluralStringResource(
        @PluralsRes val resId: Int, val count: Int, vararg val args: Any
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, args)
            is PluralStringResource -> pluralStringResource(resId, count, args)
        }
    }
}
