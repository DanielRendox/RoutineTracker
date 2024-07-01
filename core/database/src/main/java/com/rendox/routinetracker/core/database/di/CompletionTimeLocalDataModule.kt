package com.rendox.routinetracker.core.database.di

import com.rendox.routinetracker.core.database.completiontime.DueDateSpecificCompletionTimeLocalDataSource
import com.rendox.routinetracker.core.database.completiontime.DueDateSpecificCompletionTimeLocalDataSourceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val completionTimeLocalDataModule = module {
    single<DueDateSpecificCompletionTimeLocalDataSource> {
        DueDateSpecificCompletionTimeLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }
}