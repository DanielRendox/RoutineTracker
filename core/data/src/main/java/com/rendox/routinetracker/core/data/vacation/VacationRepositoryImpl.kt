package com.rendox.routinetracker.core.data.vacation

import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

class VacationRepositoryImpl(
    private val localDataSource: VacationLocalDataSource
): VacationRepository {
    override suspend fun getVacationByDate(habitId: Long, date: LocalDate): Vacation? {
        return localDataSource.getVacationByDate(habitId, date)
    }

    override suspend fun getPreviousVacation(habitId: Long, currentDate: LocalDate): Vacation? {
        return localDataSource.getPreviousVacation(habitId, currentDate)
    }

    override suspend fun getLastVacation(habitId: Long): Vacation? {
        return localDataSource.getLastVacation(habitId)
    }

    override suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Vacation> {
        return localDataSource.getVacationsInPeriod(habitId, minDate, maxDate)
    }

    override suspend fun insertVacation(habitId: Long, vacation: Vacation) {
        return localDataSource.insertVacation(habitId, vacation)
    }

    override suspend fun deleteVacationById(id: Long) {
        localDataSource.deleteVacationById(id)
    }
}