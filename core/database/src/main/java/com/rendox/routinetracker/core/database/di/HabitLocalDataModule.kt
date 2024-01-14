package com.rendox.routinetracker.core.database.di

import com.rendox.routinetracker.core.database.habit.HabitLocalDataSource
import com.rendox.routinetracker.core.database.habit.HabitLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val habitLocalDataModule = module {
    single<HabitLocalDataSource> {
        HabitLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }
}