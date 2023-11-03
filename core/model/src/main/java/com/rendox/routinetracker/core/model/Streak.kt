package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

data class Streak(
    val start: LocalDate,
    val end: LocalDate?,
)
