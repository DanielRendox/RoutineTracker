package com.rendox.routinetracker.core.model

sealed class Routine {
    abstract val id: Long?
    abstract val name: String
    abstract val schedule: Schedule

    data class YesNoRoutine(
        override val id: Long? = null,
        override val name: String,
        override val schedule: Schedule,
        val scheduleDeviation: Int = 0,
    ) : Routine()
}