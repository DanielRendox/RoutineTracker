package com.rendox.routinetracker.addeditroutine.schedulepicker.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.rendox.routinetracker.feature.addeditroutine.R

@Immutable
sealed class ScheduleTypeUi(
    @StringRes val titleId: Int,
) {
    data object EveryDaySchedule : ScheduleTypeUi(titleId = R.string.schedule_type_label_every_day)
    data object WeeklySchedule : ScheduleTypeUi(titleId = R.string.schedule_type_label_weekly)
    data object MonthlySchedule : ScheduleTypeUi(titleId = R.string.schedule_type_label_monthly)
    data object AlternateDaysSchedule : ScheduleTypeUi(titleId = R.string.schedule_type_label_alternate_days)
}