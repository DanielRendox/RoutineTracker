package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.completionhistory.CompletionHistoryRepositoryImpl
import com.rendox.routinetracker.core.database.completionhistory.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.completionhistory.CompletionHistoryLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val completionHistoryDataModule = module {
    single<CompletionHistoryLocalDataSource> {
        CompletionHistoryLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }

    single<CompletionHistoryRepository> {
        CompletionHistoryRepositoryImpl(localDataSource = get())
    }
}