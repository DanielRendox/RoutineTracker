package com.rendox.routinetracker.core.data.completion_history

import kotlinx.datetime.LocalDate

interface FailHistoryRepository {
    suspend fun insertFail(
        habitId: Long,
        date: LocalDate,
    )

    suspend fun deleteFailByDate(
        habitId: Long,
        date: LocalDate,
    )
}