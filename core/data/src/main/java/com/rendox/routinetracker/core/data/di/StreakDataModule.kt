package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.streaks.StreakRepository
import com.rendox.routinetracker.core.data.streaks.StreakRepositoryImpl
import org.koin.dsl.module

val streakDataModule = module {
    single<StreakRepository> {
        StreakRepositoryImpl(localDataSource = get())
    }
}