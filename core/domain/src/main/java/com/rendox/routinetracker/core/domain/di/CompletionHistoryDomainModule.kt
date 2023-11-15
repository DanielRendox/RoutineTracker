package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.completion_history.ToggleRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.GetListOfStreaksUseCase
import com.rendox.routinetracker.core.domain.completion_history.GetRoutineStatusUseCase
import com.rendox.routinetracker.core.domain.completion_history.InsertRoutineStatusUseCase
import org.koin.dsl.module

val completionHistoryDomainModule = module {

    single {
        GetListOfStreaksUseCase(
            completionHistoryRepository = get(),
            routineRepository = get(),
        )
    }

    single {
        InsertRoutineStatusUseCase(
            completionHistoryRepository = get(),
            routineRepository = get(),
        )
    }

    single {
        GetRoutineStatusUseCase(
            routineRepository = get(),
            completionHistoryRepository = get(),
            insertRoutineStatus = get(),
        )
    }

    single {
        ToggleRoutineStatusUseCase(
            completionHistoryRepository = get(),
            routineRepository = get(),
        )
    }
}