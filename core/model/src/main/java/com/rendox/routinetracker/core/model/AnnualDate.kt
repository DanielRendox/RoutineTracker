package com.rendox.routinetracker.core.model

import kotlinx.datetime.Month

data class AnnualDate(
    val month: Month,
    val dayOfMonth: Int,
)