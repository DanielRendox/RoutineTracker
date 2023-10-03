package com.rendox.routinetracker.core.data.completion_history

import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSourceImpl
import org.koin.dsl.module

val completionHistoryModule = module {

    single<CompletionHistoryLocalDataSource> {
        CompletionHistoryLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<CompletionHistoryRepository> {
        CompletionHistoryRepositoryImpl(localDataSource = get())
    }
}