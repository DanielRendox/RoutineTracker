package com.rendox.routinetracker.addeditroutine.schedulepicker.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rendox.routinetracker.addeditroutine.schedulepicker.components.ScheduleTypeUi

sealed class SchedulePickerState(
    selected: Boolean,
) {
    abstract val scheduleType: ScheduleTypeUi

    var selected: Boolean by mutableStateOf(selected)
        private set

    open fun updateSelected(selected: Boolean) {
        this.selected = selected
    }
}