package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSourceImpl
import com.rendox.routinetracker.core.data.vacation.VacationRepository
import com.rendox.routinetracker.core.data.vacation.VacationRepositoryImpl
import com.rendox.routinetracker.core.database.vacation.VacationLocalDataSource
import org.koin.core.qualifier.named
import org.koin.dsl.module

val vacationDataModule = module {
    single<VacationLocalDataSource> {
        VacationLocalDataSourceImpl(
            db = get(),
            ioDispatcher = get(qualifier = named("ioDispatcher")),
        )
    }
    single<VacationRepository> {
        VacationRepositoryImpl(
            localDataSource = get(),
        )
    }
}