package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class VacationRepositoryFake(
    private val habitData: HabitData
) : VacationRepository {

    override suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Vacation> = habitData.vacationHistory.value.filter {
        val vacationEndDate = it.second.endDate
        it.first == habitId &&
                (minDate == null || minDate <= it.second.startDate || it.second.containsDate(minDate)) &&
                (maxDate == null || (vacationEndDate == null && it.second.startDate <= maxDate) || (vacationEndDate != null && vacationEndDate <= maxDate) || it.second.containsDate(maxDate))

    }.map { it.second }

    override suspend fun getAllVacations(): Map<Long, List<Vacation>> {
        return habitData.vacationHistory.value.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second },
        )
    }

    override suspend fun insertVacation(habitId: Long, vacation: Vacation) {
        habitData.vacationHistory.update {
            it.toMutableList().apply { add(habitId to vacation) }
        }
    }

    override suspend fun deleteVacationById(id: Long) {
        habitData.vacationHistory.update {
            it.toMutableList().apply { removeAt((id - 1).toInt()) }
        }
    }
}