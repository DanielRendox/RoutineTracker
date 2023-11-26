package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.streak.StreakRepository
import com.rendox.routinetracker.core.data.streak.StreakRepositoryImpl
//import com.rendox.routinetracker.core.data.streak.StreakRepositoryImpl
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSource
import com.rendox.routinetracker.core.database.streak.StreakLocalDataSourceImpl
import org.koin.dsl.module

val streakDataModule = module {

    single<StreakLocalDataSource> {
        StreakLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<StreakRepository> {
        StreakRepositoryImpl(localDataSource = get())
    }
}