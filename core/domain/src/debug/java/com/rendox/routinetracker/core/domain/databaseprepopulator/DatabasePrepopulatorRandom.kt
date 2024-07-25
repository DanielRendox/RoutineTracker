package com.rendox.routinetracker.core.domain.databaseprepopulator

import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.database.completionhistory.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.habit.HabitLocalDataSource
import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource
import com.rendox.routinetracker.core.domain.streak.computer.StreakComputer
import com.rendox.routinetracker.core.logic.time.today
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.model.Streak
import com.rendox.routinetracker.core.model.Vacation

class DatabasePrepopulatorRandom(
    private val habitLocalDataSource: HabitLocalDataSource,
    private val completionHistoryLocalDataSource: CompletionHistoryLocalDataSource,
    private val vacationLocalDataSource: VacationLocalDataSource,
    private val streakComputer: StreakComputer,
    private val streakRepository: StreakRepository,
) : DatabasePrepopulator {
    override suspend fun prepopulateDatabase() {
        val dbIsNotEmpty = !habitLocalDataSource.checkIfIsEmpty()
        if (dbIsNotEmpty) return

        val habits = com.rendox.routinetracker.core.domain.RandomHabitsGenerator.generateRandomHabits(numOfHabits = 20)
        habitLocalDataSource.insertHabits(habits)

        val insertedHabits = habitLocalDataSource.getAllHabits()

        val completionHistory: Map<Long, List<Habit.CompletionRecord>> = insertedHabits
            .associateWith { habit ->
                com.rendox.routinetracker.core.domain.RandomHabitsGenerator.generateCompletionHistory(habit)
            }
            .mapKeys { it.key.id!! }
        completionHistoryLocalDataSource.insertCompletions(completionHistory)

        val vacationHistory: Map<Long, List<Vacation>> = insertedHabits
            .associateWith { habit ->
                com.rendox.routinetracker.core.domain.RandomHabitsGenerator.generateVacationHistory(habit)
            }
            .mapKeys { it.key.id!! }
        vacationLocalDataSource.insertVacations(vacationHistory)

        val streaksMap: Map<Long, List<Streak>> = insertedHabits.associateBy(
            keySelector = { it.id!! },
            valueTransform = { habit ->
                val habitId = habit.id!!
                streakComputer.computeStreaks(
                    habit = habit,
                    completionHistory = completionHistory[habitId]!!,
                    vacationHistory = vacationHistory[habitId]!!,
                    today = today,
                )
            },
        )
        streakRepository.insertStreaks(streaksMap)
    }
}