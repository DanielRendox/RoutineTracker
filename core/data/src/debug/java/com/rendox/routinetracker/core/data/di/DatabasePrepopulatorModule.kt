package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.DatabasePrepopulator
import com.rendox.routinetracker.core.data.DatabasePrepopulatorRandom
import org.koin.dsl.module

val databasePrepopulatorModule = module {
    single<DatabasePrepopulator> {
        DatabasePrepopulatorRandom(
            habitLocalDataSource = get(),
            completionHistoryLocalDataSource = get(),
            vacationLocalDataSource = get(),
        )
    }
}