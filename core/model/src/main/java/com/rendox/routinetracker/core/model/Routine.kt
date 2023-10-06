package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

sealed class Routine {
    abstract val id: Long?
    abstract val name: String
    abstract val startDate: LocalDate
    abstract val backlogEnabled: Boolean
    abstract val vacationStartDate: LocalDate?
    abstract val vacationEndDate: LocalDate?
    abstract val schedule: Schedule

    data class YesNoRoutine(
        override val id: Long?,
        override val name: String,
        override val startDate: LocalDate,
        override val backlogEnabled: Boolean,
        override val vacationStartDate: LocalDate?,
        override val vacationEndDate: LocalDate?,
        override val schedule: Schedule,
    ) : Routine()
}