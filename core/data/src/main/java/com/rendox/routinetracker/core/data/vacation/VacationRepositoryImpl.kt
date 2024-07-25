package com.rendox.routinetracker.core.data.vacation

import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Vacation

class VacationRepositoryImpl(
    private val localDataSource: VacationLocalDataSource,
) : VacationRepository {

    override suspend fun getVacationsInPeriod(
        habitId: Long,
        period: LocalDateRange,
    ): List<Vacation> = localDataSource.getVacationsInPeriod(habitId, period)

    override suspend fun getMultiHabitVacations(
        habitsToPeriods: List<Pair<List<Long>, LocalDateRange>>,
    ): Map<Long, List<Vacation>> = localDataSource.getMultiHabitVacations(habitsToPeriods)

    override suspend fun insertVacation(
        habitId: Long,
        vacation: Vacation,
    ) = localDataSource.insertVacation(habitId, vacation)

    override suspend fun insertVacations(habitIdsToVacations: Map<Long, List<Vacation>>) =
        localDataSource.insertVacations(habitIdsToVacations)

    override suspend fun deleteVacationById(id: Long) {
        localDataSource.deleteVacationById(id)
    }
}