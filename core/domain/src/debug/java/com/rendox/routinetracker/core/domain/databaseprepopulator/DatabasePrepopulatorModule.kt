package com.rendox.routinetracker.core.domain.databaseprepopulator

import org.koin.dsl.module

val databasePrepopulatorModule = module {
    single<DatabasePrepopulator> {
        DatabasePrepopulatorNoOp()
    }
}