package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_time.GetHabitCompletionTimeUseCase
import com.rendox.routinetracker.core.domain.completion_time.GetHabitCompletionTimeUseCaseImpl
import org.koin.dsl.module

val completionTimeDomainModule = module {
    single<GetHabitCompletionTimeUseCase> {
        GetHabitCompletionTimeUseCaseImpl(
            getHabit = get(),
            dueDateSpecificCompletionTimeRepository = get(),
            completionTimeRepository = get(),
        )
    }
}