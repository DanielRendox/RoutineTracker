package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.completiontime.CompletionTimeRepository
import com.rendox.routinetracker.core.data.completiontime.CompletionTimeRepositoryImpl
import com.rendox.routinetracker.core.database.completiontime.CompletionTimeLocalDataSource
import com.rendox.routinetracker.core.database.completiontime.CompletionTimeLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val completionTimeDataModule = module {

    single<CompletionTimeLocalDataSource> {
        CompletionTimeLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }

    single<CompletionTimeRepository> {
        CompletionTimeRepositoryImpl(localDataSource = get())
    }
}