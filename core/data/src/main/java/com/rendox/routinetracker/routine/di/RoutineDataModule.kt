package com.rendox.routinetracker.routine.di

import app.cash.sqldelight.ColumnAdapter
import com.rendox.performancetracker.Database
import com.rendox.routinetracker.routine.data.RoutineLocalDataSource
import com.rendox.routinetracker.routine.data.RoutineLocalDataSourceImpl
import com.rendox.routinetracker.routine.data.RoutineRepository
import com.rendox.routinetracker.routine.data.RoutineRepositoryImpl
import org.koin.dsl.module

val routineDataModule = module {

    single<RoutineLocalDataSource> {
        RoutineLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<RoutineRepository> {
        RoutineRepositoryImpl(localDataSource = get())
    }
}