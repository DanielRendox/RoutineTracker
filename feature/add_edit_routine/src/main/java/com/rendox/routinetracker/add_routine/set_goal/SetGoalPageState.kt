package com.rendox.routinetracker.add_routine.set_goal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class SetGoalPageState(
    routineName: String = "",
    routineDescription: String? = null,
    routineNameIsValid: Boolean = true,
) {
    var routineName by mutableStateOf(routineName)
        private set

    var routineDescription by mutableStateOf(routineDescription)
        private set

    var routineNameIsValid by mutableStateOf(routineNameIsValid)
        private set

    var containsError by mutableStateOf(false)
        private set

    private val _routineNameIsValid
        get() = routineName != ""

    init {
        updateContainsErrorState()
    }

    fun updateRoutineName(newName: String) {
        routineName = newName
        routineNameIsValid = _routineNameIsValid
        updateContainsErrorState()
    }

    fun updateRoutineDescription(newDescription: String) {
        if (newDescription == "") {
            routineDescription = null
            return
        }
        routineDescription = newDescription
    }

    fun checkIfContainsErrors(): Boolean {
        routineNameIsValid = _routineNameIsValid
        updateContainsErrorState()
        return containsError
    }

    private fun updateContainsErrorState() {
        containsError = !routineNameIsValid
    }

    companion object {
        val Saver: Saver<SetGoalPageState, *> = listSaver(
            save = { setGoalScreenState ->
                listOf(
                    setGoalScreenState.routineName,
                    setGoalScreenState.routineDescription,
                )
            },
            restore = { setGoalScreenStateValues ->
                SetGoalPageState(
                    routineName = setGoalScreenStateValues[0] as String,
                    routineDescription = setGoalScreenStateValues[1],
                )
            }
        )
    }
}

@Composable
fun rememberSetGoalPageState() =
    rememberSaveable(saver = SetGoalPageState.Saver) {
        SetGoalPageState()
    }