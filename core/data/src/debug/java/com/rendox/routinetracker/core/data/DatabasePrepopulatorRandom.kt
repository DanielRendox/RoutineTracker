package com.rendox.routinetracker.core.data

import com.rendox.routinetracker.core.database.completionhistory.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.habit.HabitLocalDataSource
import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource

class DatabasePrepopulatorRandom(
    private val habitLocalDataSource: HabitLocalDataSource,
    private val completionHistoryLocalDataSource: CompletionHistoryLocalDataSource,
    private val vacationLocalDataSource: VacationLocalDataSource,
) : DatabasePrepopulator {
    override suspend fun prepopulateDatabase() {
        val dbIsNotEmpty = !habitLocalDataSource.checkIfIsEmpty()
        if (dbIsNotEmpty) return

        val habits = RandomHabitsGenerator.generateRandomHabits(numOfHabits = 20)
        habitLocalDataSource.insertHabits(habits)

        val insertedHabits = habitLocalDataSource.getAllHabits()

        val completionHistory = insertedHabits
            .associateWith { habit -> RandomHabitsGenerator.generateCompletionHistory(habit) }
            .mapKeys { it.key.id!! }
        completionHistoryLocalDataSource.insertCompletions(completionHistory)

        val vacationHistory = insertedHabits
            .associateWith { habit -> RandomHabitsGenerator.generateVacationHistory(habit) }
            .mapKeys { it.key.id!! }
        vacationLocalDataSource.insertVacations(vacationHistory)
    }
}