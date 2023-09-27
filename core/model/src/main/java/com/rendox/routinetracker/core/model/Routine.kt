package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

data class Routine(
    val id: Long,
    val type: RoutineType,
    val name: String,
    val startDate: LocalDate,
    val backlogEnabled: Boolean,
    val periodSeparation: Boolean,
    val vacationStartDate: LocalDate?,
    val vacationEndDate: LocalDate?,
)