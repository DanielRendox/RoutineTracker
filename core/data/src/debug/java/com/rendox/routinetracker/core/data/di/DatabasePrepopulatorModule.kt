package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.DatabasePrepopulator
import com.rendox.routinetracker.core.data.DatabasePrepopulatorRandom
import com.rendox.routinetracker.core.data.RandomHabitsGenerator
import com.rendox.routinetracker.core.logic.time.rangeTo
import kotlinx.datetime.LocalDate
import org.koin.dsl.module

val databasePrepopulatorModule = module {
    single<DatabasePrepopulator> {
        DatabasePrepopulatorRandom(
            habitsGenerator = RandomHabitsGenerator(
                numOfHabits = 50,
                startDateRange = LocalDate(2010, 1, 1)..LocalDate(2012, 1, 1),
            ),
            habitLocalDataSource = get(),
            completionHistoryLocalDataSource = get(),
            vacationLocalDataSource = get(),
        )
    }
}