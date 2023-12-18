package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalTime

sealed class Habit {
    abstract val id: Long?
    abstract val name: String
    abstract val description: String?
    abstract val sessionDurationMinutes: Int?
    abstract val progress: Float?
    abstract val schedule: Schedule
    abstract val defaultCompletionTime: LocalTime?

    data class YesNoHabit(
        override val id: Long? = null,
        override val name: String,
        override val description: String? = null,
        override val sessionDurationMinutes: Int? = null,
        override val progress: Float? = null,
        override val schedule: Schedule,
        override val defaultCompletionTime: LocalTime? = null,
    ) : Habit()
}