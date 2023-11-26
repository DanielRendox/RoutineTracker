package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

data class Streak(
    val id: Long? = null,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

data class DisplayStreak(
    val startDate: LocalDate,
    val endDate: LocalDate,
)
