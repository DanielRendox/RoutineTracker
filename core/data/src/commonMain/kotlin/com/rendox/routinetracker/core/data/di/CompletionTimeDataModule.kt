package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.completion_time.CompletionTimeRepository
import com.rendox.routinetracker.core.data.completion_time.CompletionTimeRepositoryImpl
import com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSource
import com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val completionTimeDataModule = module {

    single<com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSource> {
        com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }

    single<CompletionTimeRepository> {
        CompletionTimeRepositoryImpl(localDataSource = get())
    }
}