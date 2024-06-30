package com.rendox.routinetracker.add_edit_routine.choose_routine_type

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class ChooseRoutineTypePageState(
    routineType: RoutineTypeUi = RoutineTypeUi.YesNoHabit,
) {
    var routineType: RoutineTypeUi by mutableStateOf(routineType)
        private set

    fun updateRoutineType(newType: RoutineTypeUi) {
        routineType = newType
    }

    companion object {
        val Saver: Saver<ChooseRoutineTypePageState, *> = listSaver(
            save = { chooseRoutineTypePageState ->
                listOf(chooseRoutineTypePageState.routineType.routineTypeId)
            },
            restore = { chooseRoutineTypeStateValues ->
                ChooseRoutineTypePageState(
                    routineType = RoutineTypeUi.getTypeById(chooseRoutineTypeStateValues[0]),
                )
            },
        )
    }
}

@Composable
fun rememberChooseRoutineTypePageState() = rememberSaveable(saver = ChooseRoutineTypePageState.Saver) {
    ChooseRoutineTypePageState()
}