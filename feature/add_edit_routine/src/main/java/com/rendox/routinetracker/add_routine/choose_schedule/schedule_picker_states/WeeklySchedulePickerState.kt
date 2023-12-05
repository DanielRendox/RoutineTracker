package com.rendox.routinetracker.add_routine.choose_schedule.schedule_picker_states

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kizitonwose.calendar.core.daysOfWeek
import com.rendox.routinetracker.add_routine.choose_schedule.schedule_pickers.ScheduleTypeUi
import kotlinx.datetime.DayOfWeek

@Stable
class WeeklySchedulePickerState(
    numOfDueDays: String = "1",
    specificDaysOfWeek: List<DayOfWeek> = emptyList(),
    selected: Boolean = false,
    chooseSpecificDays: Boolean = false,
) : TraditionalPeriodSchedulePickerState(
    numOfDueDays = numOfDueDays,
    selected = selected,
    chooseSpecificDays = chooseSpecificDays,
) {
    override val scheduleType = ScheduleTypeUi.WeeklySchedule

    var specificDaysOfWeek by mutableStateOf(specificDaysOfWeek)
        private set

    init {
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    fun updateNumOfDueDays(newNumber: String) {
        if (newNumber.length <= 1) numOfDueDays = newNumber
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    fun toggleChooseSpecificDays(startDayOfWeek: DayOfWeek) {
        chooseSpecificDays = !chooseSpecificDays
        numOfDueDaysTextFieldIsEditable = !chooseSpecificDays

        if (chooseSpecificDays && specificDaysOfWeek.isEmpty()){
            val numOfDueDays = try {
                this.numOfDueDays.toInt()
            } catch (e: NumberFormatException) {
                1
            }
            val numOfPreSelectedDays = when {
                numOfDueDays < 1 -> 1
                numOfDueDays > 7 -> 7
                else -> numOfDueDays
            }
            specificDaysOfWeek = daysOfWeek(startDayOfWeek).take(numOfPreSelectedDays)
        }

        this.numOfDueDays = specificDaysOfWeek.size.toString()
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    fun toggleDayOfWeek(dayOfWeek: DayOfWeek) {
        specificDaysOfWeek = specificDaysOfWeek.toMutableList().apply {
            if (specificDaysOfWeek.contains(dayOfWeek)) remove(dayOfWeek) else add(dayOfWeek)
        }
        numOfDueDays = specificDaysOfWeek.size.toString()
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    private fun checkNumOfDueDaysValidity() {
        val numOfDueDays = try {
            this.numOfDueDays.toInt()
        } catch (e: NumberFormatException) {
            numOfDueDaysIsValid = false
            return
        }
        numOfDueDaysIsValid = numOfDueDays in 1..7
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        val Saver: Saver<WeeklySchedulePickerState, *> = listSaver(
            save = { weeklySchedulePickerState ->
                listOf(
                    weeklySchedulePickerState.numOfDueDays,
                    weeklySchedulePickerState.specificDaysOfWeek,
                    weeklySchedulePickerState.numOfDueDaysIsValid,
                    weeklySchedulePickerState.selected,
                    weeklySchedulePickerState.chooseSpecificDays,
                )
            },
            restore = { weeklySchedulePickerStateValues ->
                WeeklySchedulePickerState(
                    numOfDueDays = weeklySchedulePickerStateValues[0] as String,
                    specificDaysOfWeek = weeklySchedulePickerStateValues[1] as List<DayOfWeek>,
                    selected = weeklySchedulePickerStateValues[3] as Boolean,
                    chooseSpecificDays = weeklySchedulePickerStateValues[4] as Boolean,
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