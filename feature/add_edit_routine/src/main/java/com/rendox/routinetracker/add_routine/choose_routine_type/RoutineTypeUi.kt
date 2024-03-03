package com.rendox.routinetracker.add_routine.choose_routine_type

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.rendox.routinetracker.feature.agenda.R

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

    data object MeasurableHabit : RoutineTypeUi(
        routineTypeId = 2,
        titleId = R.string.measurable_habit_title,
        descriptionId = R.string.measurable_habit_description,
        inDevelopment = true,
    )

    companion object {
        fun getTypeById(id: Int) = when (id) {
            YesNoHabit.routineTypeId -> YesNoHabit
            MeasurableHabit.routineTypeId -> MeasurableHabit
            else -> throw IllegalArgumentException()
        }
    }
}

val habitTypes = listOf(
    RoutineTypeUi.YesNoHabit,
    RoutineTypeUi.MeasurableHabit,
)