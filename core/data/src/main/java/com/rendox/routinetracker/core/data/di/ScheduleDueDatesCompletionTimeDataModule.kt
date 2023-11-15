package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.routine.schedule.due_dates_completion_time.ScheduleDueDatesCompletionTimeRepository
import com.rendox.routinetracker.core.data.routine.schedule.due_dates_completion_time.ScheduleDueDatesCompletionTimeRepositoryImpl
import com.rendox.routinetracker.core.database.routine.schedule.due_dates_completion_time.ScheduleDueDatesCompletionTimeLocalDataSource
import com.rendox.routinetracker.core.database.routine.schedule.due_dates_completion_time.ScheduleDueDatesCompletionTimeLocalDataSourceImpl
import org.koin.dsl.module

val scheduleDueDatesCompletionTimeDataModule = module {

    single<ScheduleDueDatesCompletionTimeLocalDataSource> {
        ScheduleDueDatesCompletionTimeLocalDataSourceImpl(db = get(), dispatcher = get())
    }

    single<ScheduleDueDatesCompletionTimeRepository> {
        ScheduleDueDatesCompletionTimeRepositoryImpl(localDataSource = get())
    }
}