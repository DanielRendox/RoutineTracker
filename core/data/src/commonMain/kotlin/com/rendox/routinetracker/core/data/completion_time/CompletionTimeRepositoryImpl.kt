package com.rendox.routinetracker.core.data.completion_time

import com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class CompletionTimeRepositoryImpl(
    private val localDataSource: com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSource,
) : CompletionTimeRepository {

    override suspend fun getCompletionTime(routineId: Long, date: LocalDate): LocalTime? {
        return localDataSource.getCompletionTime(routineId, date)
    }

    override suspend fun updateCompletionTime(routineId: Long, date: LocalDate, time: LocalTime) {
        localDataSource.updateCompletionTime(routineId, date, time)
    }

    override suspend fun insertCompletionTime(id: Long?, routineId: Long, date: LocalDate, time: LocalTime) {
        localDataSource.insertCompletionTime(id, routineId, date, time)
    }

    override suspend fun deleteCompletionTime(routineId: Long, date: LocalDate) {
        localDataSource.deleteCompletionTime(routineId, date)
    }
}