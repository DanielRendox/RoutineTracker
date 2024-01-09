package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.completion_time.CompletionTimeRepository
import com.rendox.routinetracker.core.data.completion_time.CompletionTimeRepositoryImpl
import com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSource
import com.rendox.routinetracker.core.database.completion_time.CompletionTimeLocalDataSourceImpl
import org.koin.dsl.module

val completionTimeDataModule = module {

    single<CompletionTimeLocalDataSource> {
        CompletionTimeLocalDataSourceImpl(db = get())
    }

    single<CompletionTimeRepository> {
        CompletionTimeRepositoryImpl(localDataSource = get())
    }
}