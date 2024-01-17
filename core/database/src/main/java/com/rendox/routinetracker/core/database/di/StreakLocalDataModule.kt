package com.rendox.routinetracker.core.database.di

import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val streakLocalDataModule = module {
    single<StreakLocalDataSource> {
        StreakLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }
}