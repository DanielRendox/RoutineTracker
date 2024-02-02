package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.habit.HabitRepository
import com.rendox.routinetracker.core.data.habit.HabitRepositoryImpl
import org.koin.dsl.module

val routineDataModule = module {

    single<HabitRepository> {
        HabitRepositoryImpl(localDataSource = get())
    }
}