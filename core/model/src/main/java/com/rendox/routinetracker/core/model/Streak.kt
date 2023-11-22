package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

data class Streak(
    val id: Long? = null,
    val start: LocalDate,
    val end: LocalDate?,
)
