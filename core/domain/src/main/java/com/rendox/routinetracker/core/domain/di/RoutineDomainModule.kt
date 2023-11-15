package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.routine.GetRoutineCompletionTimeUseCase
import org.koin.dsl.module

val routineDomainModule = module {

    single {
        GetRoutineCompletionTimeUseCase(
            routineRepository = get(),
            completionTimeRepository = get(),
            scheduleDueDatesCompletionTimeRepository = get(),
        )
    }
}