package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepository
import com.rendox.routinetracker.core.data.completion_history.CompletionHistoryRepositoryImpl
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSourceImpl
import org.koin.dsl.module

val completionHistoryDataModule = module {

    single<CompletionHistoryLocalDataSource> {
        CompletionHistoryLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<CompletionHistoryRepository> {
        CompletionHistoryRepositoryImpl(localDataSource = get())
    }
}