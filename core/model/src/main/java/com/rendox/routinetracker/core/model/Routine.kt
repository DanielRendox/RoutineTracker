package com.rendox.routinetracker.core.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime

sealed class Routine {
    abstract val id: Long?
    abstract val name: String
    abstract val description: String?
    abstract val sessionDurationMinutes: Int?
    abstract val progress: Float?
    abstract val schedule: Schedule
    abstract val defaultCompletionTime: LocalTime?

    data class YesNoRoutine(
        override val id: Long? = null,
        override val name: String,
        override val description: String? = null,
        override val sessionDurationMinutes: Int? = null,
        override val progress: Float? = null,
        override val schedule: Schedule,
        override val defaultCompletionTime: LocalTime? = null,
    ) : Routine()

    data class NumericalValueRoutineUnit(
        val numOfUnitsPerSession: Int,
        val unitsOfMeasure: String,
        val sessionUnit: DateTimeUnit,
    )
}