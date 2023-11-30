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
    object YesNoRoutine : RoutineTypeUi(
        routineTypeId = 1,
        titleId = R.string.yes_no_routine_title,
        descriptionId = R.string.yes_no_routine_description,
        inDevelopment = false,
    )

    object MeasurableRoutine : RoutineTypeUi(
        routineTypeId = 2,
        titleId = R.string.measurable_routine_title,
        descriptionId = R.string.measurable_routine_description,
        inDevelopment = true,
    )

    companion object {
        fun getTypeById(id: Int) = when (id) {
            YesNoRoutine.routineTypeId -> YesNoRoutine
            MeasurableRoutine.routineTypeId -> MeasurableRoutine
            else -> throw IllegalArgumentException()
        }
    }
}

val routineTypes = listOf(
    RoutineTypeUi.YesNoRoutine,
    RoutineTypeUi.MeasurableRoutine,
)