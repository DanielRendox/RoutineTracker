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
import com.rendox.routinetracker.core.model.Schedule

@Stable
class MonthlySchedulePickerState(
    numOfDueDays: String = "1",
    specificDaysOfMonth: List<Int> = emptyList(),
    lastDayOfMonthSelected: Boolean = false,
    selected: Boolean = false,
    chooseSpecificDays: Boolean = false,
) : TraditionalPeriodSchedulePickerState(
    numOfDueDays = numOfDueDays,
    selected = selected,
    chooseSpecificDays = chooseSpecificDays,
) {
    override val scheduleType = ScheduleTypeUi.MonthlySchedule

    var specificDaysOfMonth by mutableStateOf(specificDaysOfMonth)
        private set

    var lastDayOfMonthSelected by mutableStateOf(lastDayOfMonthSelected)
        private set

    init {
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    fun updateNumOfDueDays(newNumber: String) {
        if (newNumber.length <= 2) numOfDueDays = newNumber
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    fun toggleChooseSpecificDays() {
        chooseSpecificDays = !chooseSpecificDays
        numOfDueDaysTextFieldIsEditable = !chooseSpecificDays

        if (chooseSpecificDays && specificDaysOfMonth.isEmpty() && !lastDayOfMonthSelected) {
            val numOfDueDays = try {
                this.numOfDueDays.toInt()
            } catch (e: NumberFormatException) {
                this.numOfDueDays = "1"
                1
            }
            val numOfPreSelectedDays = when {
                numOfDueDays < 1 -> 1
                numOfDueDays > 31 -> 31
                else -> numOfDueDays
            }
            specificDaysOfMonth = (1..numOfPreSelectedDays).toList()
        }

        val numOfDueDays = specificDaysOfMonth.size + if (lastDayOfMonthSelected) 1 else 0
        this.numOfDueDays = numOfDueDays.toString()
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    fun toggleDayOfMonth(dayOfMonth: Int) {
        val numOfDueDays = this.numOfDueDays.toInt()
        specificDaysOfMonth = specificDaysOfMonth.toMutableList().also {
            if (specificDaysOfMonth.contains(dayOfMonth)) {
                this.numOfDueDays = (numOfDueDays - 1).toString()
                it.remove(dayOfMonth)
            } else {
                this.numOfDueDays = (numOfDueDays + 1).toString()
                it.add(dayOfMonth)
            }
        }
        checkNumOfDueDaysValidity()
        updateContainsErrorState()
    }

    fun toggleLastDayOfMonth() {
        lastDayOfMonthSelected = !lastDayOfMonthSelected
        val numOfDueDays = this.numOfDueDays.toInt()
        this.numOfDueDays =
            if (lastDayOfMonthSelected) (numOfDueDays + 1).toString()
            else (numOfDueDays - 1).toString()
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
        numOfDueDaysIsValid = numOfDueDays in 1..31
    }

    fun updateSelectedSchedule(schedule: Schedule.MonthlySchedule) {
        when (schedule) {
            is Schedule.MonthlyScheduleByNumOfDueDays -> {
                numOfDueDays = schedule.numOfDueDays.toString()
                specificDaysOfMonth = emptyList()
                chooseSpecificDays = false
                numOfDueDaysTextFieldIsEditable = true
                lastDayOfMonthSelected = false
            }
            is Schedule.MonthlyScheduleByDueDatesIndices -> {
                numOfDueDays = schedule.dueDatesIndices.size.toString()
                specificDaysOfMonth = schedule.dueDatesIndices
                chooseSpecificDays = true
                numOfDueDaysTextFieldIsEditable = false
                lastDayOfMonthSelected = schedule.includeLastDayOfMonth
            }
        }

        numOfDueDaysIsValid = true
        updateContainsErrorState()
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        val Saver: Saver<MonthlySchedulePickerState, *> = listSaver(
            save = { monthlySchedulePickerState ->
                listOf(
                    monthlySchedulePickerState.numOfDueDays,
                    monthlySchedulePickerState.specificDaysOfMonth,
                    monthlySchedulePickerState.lastDayOfMonthSelected,
                    monthlySchedulePickerState.selected,
                    monthlySchedulePickerState.chooseSpecificDays,
                )
            },
            restore = { monthlySchedulePickerStateValues ->
                MonthlySchedulePickerState(
                    numOfDueDays = monthlySchedulePickerStateValues[0] as String,
                    specificDaysOfMonth = monthlySchedulePickerStateValues[1] as List<Int>,
                    lastDayOfMonthSelected = monthlySchedulePickerStateValues[2] as Boolean,
                    selected = monthlySchedulePickerStateValues[3] as Boolean,
                    chooseSpecificDays = monthlySchedulePickerStateValues[4] as Boolean,
                )
            }
        )
    }
}

@Composable
fun rememberMonthlySchedulePickerState() =
    rememberSaveable(saver = MonthlySchedulePickerState.Saver) {
        MonthlySchedulePickerState()
    }