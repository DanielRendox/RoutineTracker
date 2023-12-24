package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.habit.HabitRepositoryImpl
import com.rendox.routinetracker.core.database.habit.HabitLocalDataSource
import com.rendox.routinetracker.core.database.habit.HabitLocalDataSourceImpl
import org.koin.dsl.module

val routineDataModule = module {

    single<HabitLocalDataSource> {
        HabitLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<HabitRepository> {
        HabitRepositoryImpl(localDataSource = get())
    }
}