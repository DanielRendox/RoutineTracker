package com.rendox.routinetracker.core.database.vacation

import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.VacationEntity
import com.rendox.routinetracker.core.model.Vacation
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class VacationLocalDataSourceImpl(
    private val db: RoutineTrackerDatabase,
    private val ioDispatcher: CoroutineContext,
): VacationLocalDataSource {
    override suspend fun getVacationByDate(habitId: Long, date: LocalDate): Vacation? {
        return withContext(ioDispatcher) {
            db.vacationEntityQueries.getVacationByDate(habitId, date)
                .executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getPreviousVacation(habitId: Long, currentDate: LocalDate): Vacation? {
        return withContext(ioDispatcher) {
            val previousVacation = db.vacationEntityQueries.getPreviousVacation(
                habitId = habitId,
                currentDate = currentDate,
            ).executeAsOneOrNull()
            previousVacation?.let {
                Vacation(
                    id = it.id,
                    startDate = it.startDate,
                    endDate = it.endDate,
                )
            }
        }
    }

    override suspend fun getLastVacation(habitId: Long): Vacation? {
        return withContext(ioDispatcher) {
            db.vacationEntityQueries.getLastVacation(habitId)
                .executeAsOneOrNull()?.toExternalModel()
        }
    }

    override suspend fun getVacationsInPeriod(
        habitId: Long,
        minDate: LocalDate?,
        maxDate: LocalDate?,
    ): List<Vacation> {
        return withContext(ioDispatcher) {
            db.vacationEntityQueries.getVacationsInPeriod(
                habitId = habitId,
                minDate = minDate,
                maxDate = maxDate,
            ).executeAsList().map { it.toExternalModel() }
        }
    }

    override suspend fun getAllVacations(): List<Pair<Long, Vacation>> {
        return withContext(ioDispatcher) {
            db.vacationEntityQueries.getAllVacations().executeAsList().map {
                Pair(it.habitId, it.toExternalModel())
            }
        }
    }

    override suspend fun insertVacation(habitId: Long, vacation: Vacation) {
        return withContext(ioDispatcher) {
            db.vacationEntityQueries.insertVacation(
                habitId = habitId,
                id = vacation.id,
                startDate = vacation.startDate,
                endDate = vacation.endDate,
            )
        }
    }

    override suspend fun deleteVacationById(id: Long) {
        return withContext(ioDispatcher) {
            db.vacationEntityQueries.deleteVacationById(id)
        }
    }

    private fun VacationEntity.toExternalModel() = Vacation(
        id = id,
        startDate = startDate,
        endDate = endDate,
    )
}