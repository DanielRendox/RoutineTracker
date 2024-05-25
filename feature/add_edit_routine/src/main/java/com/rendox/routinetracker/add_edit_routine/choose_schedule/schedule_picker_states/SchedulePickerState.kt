package com.rendox.routinetracker.add_edit_routine.choose_schedule.schedule_picker_states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rendox.routinetracker.add_edit_routine.choose_schedule.schedule_pickers.ScheduleTypeUi

sealed class SchedulePickerState(
    selected: Boolean
) {
    abstract val scheduleType: ScheduleTypeUi

    var selected: Boolean by mutableStateOf(selected)
        private set

    open fun updateSelected(selected: Boolean) {
        this.selected = selected
    }
}