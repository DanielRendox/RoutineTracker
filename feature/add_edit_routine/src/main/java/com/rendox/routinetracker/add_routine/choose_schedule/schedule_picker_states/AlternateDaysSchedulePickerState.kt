package com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_pickers.ScheduleTypeUi

@Stable
class AlternateDaysSchedulePickerState(
    selected: Boolean = false,
    numOfActivityDays: String = "",
    numOfRestDays: String = "",
    numOfRestDaysIsValid: Boolean = true,
    numOfActivityDaysIsValid: Boolean = true,
): SchedulePickerState(selected = selected) {
    override val scheduleType = ScheduleTypeUi.AlternateDaysSchedule

    val containsError: Boolean
        get() = !numOfActivityDaysIsValid || !numOfRestDaysIsValid

    var numOfActivityDays by mutableStateOf(numOfActivityDays)
        private set

    var numOfRestDays by mutableStateOf(numOfRestDays)
        private set

    var numOfActivityDaysIsValid by mutableStateOf(numOfActivityDaysIsValid)
        private set

    var numOfRestDaysIsValid by mutableStateOf(numOfRestDaysIsValid)
        private set

    fun updateNumOfActivityDays(numOfDays: String) {
        if (numOfDays.length <= 2) numOfActivityDays = numOfDays
        checkNumOfActivityDaysValidity()
    }
    fun updateNumOfRestDays(numOfDays: String) {
        if (numOfDays.length <= 2) numOfRestDays = numOfDays
        checkNumOfRestDaysValidity()
    }

    private fun checkNumOfActivityDaysValidity() {
        val numOfActivityDays = try {
            numOfActivityDays.toInt()
        } catch (e: NumberFormatException) {
            numOfActivityDaysIsValid = false
            return
        }
        numOfActivityDaysIsValid = numOfActivityDays > 0
    }


    private fun checkNumOfRestDaysValidity() {
        val numOfRestDays = try {
            numOfRestDays.toInt()
        } catch (e: NumberFormatException) {
            numOfRestDaysIsValid = false
            return
        }
        numOfRestDaysIsValid = numOfRestDays > 0
    }

    fun triggerErrorsIfAny() {
        checkNumOfActivityDaysValidity()
        checkNumOfRestDaysValidity()
    }

    override fun updateSelected(selected: Boolean) {
        super.updateSelected(selected)
        numOfActivityDaysIsValid = true
        numOfRestDaysIsValid = true
    }

    companion object {
        val Saver: Saver<AlternateDaysSchedulePickerState, *> = listSaver(
            save = { customSchedulePickerState ->
                listOf(
                    customSchedulePickerState.selected,
                    customSchedulePickerState.numOfActivityDays,
                    customSchedulePickerState.numOfRestDays,
                    customSchedulePickerState.numOfActivityDaysIsValid,
                    customSchedulePickerState.numOfRestDaysIsValid,
                )
            },
            restore = { customSchedulePickerStateValues ->
                AlternateDaysSchedulePickerState(
                    selected = customSchedulePickerStateValues[0] as Boolean,
                    numOfActivityDays = customSchedulePickerStateValues[1] as String,
                    numOfRestDays = customSchedulePickerStateValues[2] as String,
                    numOfRestDaysIsValid = customSchedulePickerStateValues[3] as Boolean,
                    numOfActivityDaysIsValid = customSchedulePickerStateValues[4] as Boolean,
                )
            }
        )
    }
}

@Composable
fun rememberAlternateDaysSchedulePickerState() =
    rememberSaveable(saver = AlternateDaysSchedulePickerState.Saver) {
        AlternateDaysSchedulePickerState()
    }