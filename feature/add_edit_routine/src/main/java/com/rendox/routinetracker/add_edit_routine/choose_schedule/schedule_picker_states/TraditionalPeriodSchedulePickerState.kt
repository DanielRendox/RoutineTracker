package com.rendox.routinetracker.add_edit_routine.choose_schedule.schedule_picker_states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class TraditionalPeriodSchedulePickerState(
    numOfDueDays: String = "1",
    selected: Boolean,
    chooseSpecificDays: Boolean,
) : SchedulePickerState(selected = selected) {

    var numOfDueDays: String by mutableStateOf(numOfDueDays)
        protected set

    var numOfDueDaysTextFieldIsEditable: Boolean by mutableStateOf(true)
        protected set

    var chooseSpecificDays by mutableStateOf(chooseSpecificDays)
        protected set

    var numOfDueDaysIsValid by mutableStateOf(true)
        protected set

    var containsError by mutableStateOf(false)
        private set

    init {
        numOfDueDaysTextFieldIsEditable = !this.chooseSpecificDays
    }

    protected fun updateContainsErrorState() {
        containsError = !numOfDueDaysIsValid
    }
}