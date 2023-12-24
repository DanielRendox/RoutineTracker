package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.routine.HabitRepository
import com.rendox.routinetracker.core.data.routine.HabitRepositoryImpl
import com.rendox.routinetracker.core.database.routine.HabitLocalDataSource
import com.rendox.routinetracker.core.database.routine.HabitLocalDataSourceImpl
import org.koin.dsl.module

val routineDataModule = module {

    single<HabitLocalDataSource> {
        HabitLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<HabitRepository> {
        HabitRepositoryImpl(localDataSource = get())
    }
}