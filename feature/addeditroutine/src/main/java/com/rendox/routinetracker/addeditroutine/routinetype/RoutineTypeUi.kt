package com.rendox.routinetracker.addeditroutine.routinetype

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.rendox.routinetracker.feature.addeditroutine.R

@Immutable
sealed class RoutineTypeUi(
    val routineTypeId: Int,
    @StringRes val titleId: Int,
    @StringRes val descriptionId: Int,
    val inDevelopment: Boolean,
) {
    data object YesNoHabit : RoutineTypeUi(
        routineTypeId = 1,
        titleId = R.string.yes_no_habit_title,
        descriptionId = R.string.yes_no_habit_description,
        inDevelopment = false,
    )

    companion object {
        fun getTypeById(id: Int) = when (id) {
            YesNoHabit.routineTypeId -> YesNoHabit
            else -> throw IllegalArgumentException()
        }
    }
}

val habitTypes = listOf(
    RoutineTypeUi.YesNoHabit,
)