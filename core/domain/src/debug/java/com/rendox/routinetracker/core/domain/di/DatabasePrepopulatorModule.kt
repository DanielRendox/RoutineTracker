package com.rendox.routinetracker.core.domain.di

import com.rendox.routinetracker.core.domain.databaseprepopulator.DatabasePrepopulator
import com.rendox.routinetracker.core.domain.databaseprepopulator.DatabasePrepopulatorNoOp
import org.koin.dsl.module

val databasePrepopulatorModule = module {
    single<DatabasePrepopulator> {
        DatabasePrepopulatorNoOp()
    }
}