package com.rendox.routinetracker.core.ui.helpers

import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.ui.R

fun HabitStatus.getStringResourceId(): Int = when (this) {
    HabitStatus.Planned -> R.string.habit_status_planned
    HabitStatus.Backlog -> R.string.habit_status_backlog
    HabitStatus.NotDue -> R.string.habit_status_not_due
    HabitStatus.AlreadyCompleted -> R.string.habit_status_already_completed
    HabitStatus.CompletedLater -> R.string.habit_status_completed_later
    HabitStatus.Completed -> R.string.habit_status_completed
    HabitStatus.PartiallyCompleted -> R.string.habit_status_partially_completed
    HabitStatus.OverCompleted -> R.string.habit_status_over_completed
    HabitStatus.SortedOutBacklog -> R.string.habit_status_sorted_out_backlog
    HabitStatus.Failed -> R.string.habit_status_failed
    HabitStatus.OnVacation -> R.string.habit_status_on_vacation
    HabitStatus.NotStarted -> R.string.habit_status_not_started
    HabitStatus.Finished -> R.string.habit_status_finished
}