package com.rendox.routinetracker.add_edit_routine.tweak_routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.rendox.routinetracker.core.model.Schedule
import com.rendox.routinetracker.core.ui.helpers.UiEvent
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlinx.datetime.DayOfWeek

@Stable
class TweakRoutinePageState(
    startDate: LocalDate = LocalDate.now(),
    endDate: LocalDate? = null,
    overallNumOfDays: String = "",
    overallNumOfDaysIsValid: Boolean = true,
    sessionDuration: Duration? = null,
    sessionTime: LocalTime? = null,
    backlogEnabled: Boolean = false,
    completingAheadEnabled: Boolean = false,
    periodSeparationEnabled: Boolean? = null,
    weekStartDay: DayOfWeek? = null,
    weekStartDaySettingIsEnabled: Boolean = false,
    scheduleSupportsScheduleDeviation: Boolean = false,
    dialogType: TweakRoutinePageDialogType? = null,
) {
    var startDate by mutableStateOf(startDate)
        private set

    var endDate by mutableStateOf(endDate)
        private set

    var overallNumOfDays by mutableStateOf(overallNumOfDays)
        private set

    var sessionDuration by mutableStateOf(sessionDuration)
        private set

    var sessionTime by mutableStateOf(sessionTime)
        private set

    var backlogEnabled by mutableStateOf(backlogEnabled)
        private set

    var completingAheadEnabled by mutableStateOf(completingAheadEnabled)
        private set

    var periodSeparationEnabled by mutableStateOf(periodSeparationEnabled)
        private set

    var overallNumOfDaysIsValid by mutableStateOf(overallNumOfDaysIsValid)
        private set

    var dialogType: TweakRoutinePageDialogType? by mutableStateOf(dialogType)
        private set

    var weekStartDay by mutableStateOf(weekStartDay)
        private set

    var weekStartDaySettingIsEnabled by mutableStateOf(weekStartDaySettingIsEnabled)
        private set

    var scheduleSupportsScheduleDeviation by mutableStateOf(scheduleSupportsScheduleDeviation)
        private set

    val containsError: Boolean
        get() = !overallNumOfDaysIsValid

    var scheduleConvertedEvent by mutableStateOf<UiEvent<Schedule>?>(null)
        private set

    fun updateStartDate(startDate: LocalDate) {
        this.startDate = startDate
    }

    fun updateEndDate(endDate: LocalDate) {
        this.endDate = endDate
        this.overallNumOfDays = (ChronoUnit.DAYS.between(startDate, endDate) + 1).toString()
    }

    fun updateOverallNumOfDays(numOfDays: String) {
        if (numOfDays.length <= 4) this.overallNumOfDays = numOfDays
        updateOverallNumOfDaysValidity()
        if (overallNumOfDaysIsValid) {
            this.endDate = startDate.plusDays((numOfDays.toInt() - 1).toLong())
        }
    }

    private fun updateOverallNumOfDaysValidity() {
        val numOfDays = try {
            this.overallNumOfDays.toInt()
        } catch (e: NumberFormatException) {
            overallNumOfDaysIsValid = false
            return
        }
        overallNumOfDaysIsValid = numOfDays > 1
    }

    fun switchEndDateEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            endDate = startDate
            overallNumOfDays = "1"
        } else {
            endDate = null
            overallNumOfDays = ""
        }
    }

    fun updateDialogType(dialogType: TweakRoutinePageDialogType?) {
        this.dialogType = dialogType
    }

    fun updateBacklogEnabled(enabled: Boolean) {
        backlogEnabled = enabled
    }

    fun updateCompletingAheadEnabled(enabled: Boolean) {
        completingAheadEnabled = enabled
    }

    fun updatePeriodSeparationEnabled(enabled: Boolean) {
        periodSeparationEnabled = enabled
    }

    fun updateChosenSchedule(chosenSchedule: Schedule) {
        scheduleSupportsScheduleDeviation = chosenSchedule.supportsScheduleDeviation
        backlogEnabled = chosenSchedule.backlogEnabled
        completingAheadEnabled = chosenSchedule.completingAheadEnabled
        periodSeparationEnabled =
            if (
                chosenSchedule is Schedule.PeriodicSchedule &&
                chosenSchedule.supportsPeriodSeparation
            ) {
                chosenSchedule.periodSeparationEnabled
            } else {
                null
            }

        weekStartDaySettingIsEnabled = when (chosenSchedule) {
            is Schedule.WeeklySchedule -> true
            else -> false
        }
    }

    fun updateWeekStartDay(weekStartDay: DayOfWeek) {
        this.weekStartDay = weekStartDay
    }

    fun updateScheduleConverted(newSchedule: Schedule) {
        scheduleConvertedEvent = object : UiEvent<Schedule> {
            override val data: Schedule = newSchedule
            override fun onConsumed() {
                scheduleConvertedEvent = null
            }
        }
    }

    companion object {
        val Saver: Saver<TweakRoutinePageState, *> = listSaver(
            save = { tweakRoutinePageState ->
                listOf(
                    tweakRoutinePageState.startDate,
                    tweakRoutinePageState.endDate,
                    tweakRoutinePageState.overallNumOfDays,
                    tweakRoutinePageState.overallNumOfDaysIsValid,
                    tweakRoutinePageState.sessionDuration,
                    tweakRoutinePageState.sessionTime,
                    tweakRoutinePageState.backlogEnabled,
                    tweakRoutinePageState.completingAheadEnabled,
                    tweakRoutinePageState.periodSeparationEnabled,
                    tweakRoutinePageState.weekStartDay,
                    tweakRoutinePageState.weekStartDaySettingIsEnabled,
                    tweakRoutinePageState.scheduleSupportsScheduleDeviation,
                    tweakRoutinePageState.dialogType,
                )
            },
            restore = { tweakRoutinePageStateValues ->
                TweakRoutinePageState(
                    startDate = tweakRoutinePageStateValues[0] as LocalDate,
                    endDate = tweakRoutinePageStateValues[1] as LocalDate?,
                    overallNumOfDays = tweakRoutinePageStateValues[2] as String,
                    overallNumOfDaysIsValid = tweakRoutinePageStateValues[3] as Boolean,
                    sessionDuration = tweakRoutinePageStateValues[4] as Duration?,
                    sessionTime = tweakRoutinePageStateValues[5] as LocalTime?,
                    backlogEnabled = tweakRoutinePageStateValues[6] as Boolean,
                    completingAheadEnabled = tweakRoutinePageStateValues[7] as Boolean,
                    periodSeparationEnabled = tweakRoutinePageStateValues[8] as Boolean?,
                    weekStartDay = tweakRoutinePageStateValues[9] as DayOfWeek?,
                    weekStartDaySettingIsEnabled = tweakRoutinePageStateValues[10] as Boolean,
                    scheduleSupportsScheduleDeviation = tweakRoutinePageStateValues[11] as Boolean,
                    dialogType = tweakRoutinePageStateValues[12] as TweakRoutinePageDialogType?,
                )
            },
        )
    }
}

@Composable
fun rememberTweakRoutinePageState() = rememberSaveable(saver = TweakRoutinePageState.Saver) {
    TweakRoutinePageState()
}

enum class TweakRoutinePageDialogType {
    StartDatePicker,
    EndDatePicker,
}