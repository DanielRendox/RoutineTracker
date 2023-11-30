package com.rendox.routinetracker.add_routine.choose_schedule

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.rendox.routinetracker.feature.agenda.R

@Immutable
sealed class ScheduleTypeUi(
    val scheduleTypeId: Int,
    @StringRes val titleId: Int
) {
    object EveryDaySchedule : ScheduleTypeUi(
        scheduleTypeId = 1,
        titleId = R.string.choose_schedule_page_schedule_type_label_every_day,
    )

    companion object {
        fun getTypeById(id: Int) = when (id) {
            EveryDaySchedule.scheduleTypeId -> EveryDaySchedule
            else -> throw IllegalArgumentException()
        }
    }
}