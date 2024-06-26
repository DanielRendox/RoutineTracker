package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.DatabasePrepopulator
import com.rendox.routinetracker.core.data.DatabasePrepopulatorRandom
import com.rendox.routinetracker.core.data.RandomHabitsGenerator
import org.koin.dsl.module

val databasePrepopulatorModule = module {
    single<DatabasePrepopulator> {
        DatabasePrepopulatorRandom(
            habitsGenerator = RandomHabitsGenerator(numOfHabits = 100),
            habitLocalDataSource = get(),
            completionHistoryLocalDataSource = get(),
        )
    }
}