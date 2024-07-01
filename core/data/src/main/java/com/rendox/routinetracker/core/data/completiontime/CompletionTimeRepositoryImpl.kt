package com.rendox.routinetracker.core.data.completiontime

import com.rendox.routinetracker.core.database.completiontime.CompletionTimeLocalDataSource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class CompletionTimeRepositoryImpl(
    private val localDataSource: CompletionTimeLocalDataSource,
) : CompletionTimeRepository {

    override suspend fun getCompletionTime(
        routineId: Long,
        date: LocalDate,
    ): LocalTime? = localDataSource.getCompletionTime(routineId, date)

    override suspend fun updateCompletionTime(
        routineId: Long,
        date: LocalDate,
        time: LocalTime,
    ) {
        localDataSource.updateCompletionTime(routineId, date, time)
    }

    override suspend fun insertCompletionTime(
        id: Long?,
        routineId: Long,
        date: LocalDate,
        time: LocalTime,
    ) {
        localDataSource.insertCompletionTime(id, routineId, date, time)
    }

    override suspend fun deleteCompletionTime(
        routineId: Long,
        date: LocalDate,
    ) {
        localDataSource.deleteCompletionTime(routineId, date)
    }
}