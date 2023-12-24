package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_time.GetRoutineCompletionTimeUseCase
import org.koin.dsl.module

val routineDomainModule = module {

    single {
        GetRoutineCompletionTimeUseCase(
            habitRepository = get(),
            completionTimeRepository = get(),
        )
    }
}