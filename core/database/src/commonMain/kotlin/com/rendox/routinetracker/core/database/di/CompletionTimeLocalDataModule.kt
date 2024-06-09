package com.rendox.routinetracker.core.database.di

import com.rendox.routinetracker.core.database.completion_time.DueDateSpecificCompletionTimeLocalDataSource
import com.rendox.routinetracker.core.database.completion_time.DueDateSpecificCompletionTimeLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val completionTimeLocalDataModule = module {
    single<com.rendox.routinetracker.core.database.completion_time.DueDateSpecificCompletionTimeLocalDataSource> {
        com.rendox.routinetracker.core.database.completion_time.DueDateSpecificCompletionTimeLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }
}