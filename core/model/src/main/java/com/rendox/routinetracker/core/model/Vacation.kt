package com.rendox.routinetracker.core.model

import kotlinx.datetime.LocalDate

data class Vacation(
    val id: Long? = null,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)
