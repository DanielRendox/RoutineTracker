package com.rendox.routinetracker.core.data.vacation

import com.rendox.routinetracker.core.model.Vacation
import kotlinx.datetime.LocalDate

interface VacationRepository {

    suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
    ): List<Vacation>

    suspend fun getAllVacations(): Map<Long, List<Vacation>>
    suspend fun insertVacation(habitId: Long, vacation: Vacation)
    suspend fun deleteVacationById(id: Long)
}