package com.rendox.routinetracker.core.data.vacation

import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource
import com.rendox.routinetracker.core.logic.time.LocalDateRange
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

class VacationRepositoryImpl(
    private val localDataSource: VacationLocalDataSource,
) : VacationRepository {

    override suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate,
        maxDate: LocalDate,
    ): List<Vacation> = localDataSource.getVacationsInPeriod(habitId, minDate, maxDate)

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