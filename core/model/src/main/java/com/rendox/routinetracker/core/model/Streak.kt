package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

data class Streak(
    val startDate: LocalDate,
    val endDate: LocalDate,
)