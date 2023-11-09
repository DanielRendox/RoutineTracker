package com.rendox.routinetracker.core.database.completion_time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface CompletionTimeLocalDataSource {

    suspend fun getCompletionTime(routineId: Long, date: LocalDate): LocalTime?

    suspend fun updateCompletionTime(routineId: Long, date: LocalDate, time: LocalTime)

    suspend fun insertCompletionTime(
        id: Long? = null, routineId: Long, date: LocalDate, time: LocalTime
    )

    suspend fun deleteCompletionTime(routineId: Long, date: LocalDate)
}