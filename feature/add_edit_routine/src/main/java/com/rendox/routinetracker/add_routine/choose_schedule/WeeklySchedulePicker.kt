package com.rendox.routinetracker.add_routine.choose_schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek

@Composable
fun WeeklySchedulePicker(
    modifier: Modifier = Modifier,
    weeklySchedulePickerState: WeeklySchedulePickerState,
) {

}

@Stable
class WeeklySchedulePickerState(
    numOfDueDays: Int = 1,
    specificDaysOfWeek: List<DayOfWeek> = emptyList(),
    numOfDueDaysIsValid: Boolean = true,
) {
    var specificDaysOfWeek by mutableStateOf(specificDaysOfWeek)
        private set

    var numOfDueDays: Int by mutableIntStateOf(numOfDueDays)
        private set

    var chooseSpecificDays by mutableStateOf(false)
        private set

    var numOfDueDaysIsValid by mutableStateOf(false)
        private set

    init {
        checkNumOfDueDaysValidity()
    }

    fun updateNumOfDueDays(newNumber: Int) {
        numOfDueDays = newNumber
        checkNumOfDueDaysValidity()
    }

    private fun checkNumOfDueDaysValidity() {
        numOfDueDaysIsValid = numOfDueDays in 1..7
    }

    fun toggleChooseSpecificDays(startDayOfWeek: DayOfWeek) {
        chooseSpecificDays = !chooseSpecificDays
        specificDaysOfWeek =
            if (chooseSpecificDays) daysOfWeek(startDayOfWeek).take(numOfDueDays)
            else emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        val Saver: Saver<WeeklySchedulePickerState, *> = listSaver(
            save = { weeklySchedulePickerState ->
                listOf(
                    weeklySchedulePickerState.numOfDueDays,
                    weeklySchedulePickerState.specificDaysOfWeek,
                )
            },
            restore = { weeklySchedulePickerStateValues ->
                WeeklySchedulePickerState(
                    numOfDueDays = weeklySchedulePickerStateValues[0] as Int,
                    specificDaysOfWeek = weeklySchedulePickerStateValues[1] as List<DayOfWeek>,
                )
            },
        )
    }
}

@Composable
fun rememberWeeklySchedulePickerState() =
    rememberSaveable(saver = WeeklySchedulePickerState.Saver) {
        WeeklySchedulePickerState()
    }