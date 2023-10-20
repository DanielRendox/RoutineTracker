package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

sealed class Routine {
    abstract val id: Long?
    abstract val name: String
    abstract val schedule: Schedule
    abstract val lastDateInHistory: LocalDate?

    data class YesNoRoutine(
        override val id: Long? = null,
        override val name: String,
        override val schedule: Schedule,
        override val lastDateInHistory: LocalDate? = null,
        val scheduleDeviation: Int = 0,
    ) : Routine()
}