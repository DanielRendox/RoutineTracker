package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.routine_completion_history.RoutineCompletionHistoryRepository
import com.rendox.routinetracker.core.data.routine_completion_history.RoutineCompletionHistoryRepositoryImpl
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSource
import com.rendox.routinetracker.core.database.completion_history.CompletionHistoryLocalDataSourceImpl
import org.koin.dsl.module

val completionHistoryDataModule = module {

    single<CompletionHistoryLocalDataSource> {
        CompletionHistoryLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<RoutineCompletionHistoryRepository> {
        RoutineCompletionHistoryRepositoryImpl(localDataSource = get())
    }
}