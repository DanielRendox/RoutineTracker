package com.rendox.routinetracker.core.testcommon.fakes.habit

import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class VacationRepositoryFake(
    private val habitData: HabitData
) : VacationRepository {
    override suspend fun getVacationByDate(habitId: Long, date: LocalDate): Vacation? {
        return habitData.vacationHistory.value.firstOrNull {
            val vacationEndDate = it.second.endDate
            val dateIsWithinVacationPeriod =
                (it.second.startDate <= date && vacationEndDate != null && vacationEndDate >= date)
            it.first == habitId &&
                    (dateIsWithinVacationPeriod ||
                            ((it.second.startDate <= date && it.second.endDate == null)))
        }?.second
    }

    override suspend fun getPreviousVacation(habitId: Long, currentDate: LocalDate): Vacation? {
        return habitData.vacationHistory.value.find {
            val vacationEndDate = it.second.endDate
            it.first == habitId && vacationEndDate != null && vacationEndDate < currentDate
        }?.second
    }

    override suspend fun getLastVacation(habitId: Long): Vacation? {
        return habitData.vacationHistory.value.lastOrNull()?.second
    }

    override suspend fun insertVacation(habitId: Long, vacation: Vacation) {
        habitData.vacationHistory.update {
            it.toMutableList().apply { add(habitId to vacation) }
        }
    }

    override suspend fun deleteVacation(habitId: Long, vacation: Vacation) {
        habitData.vacationHistory.update {
            it.toMutableList().apply { remove(habitId to vacation) }
        }
    }
}