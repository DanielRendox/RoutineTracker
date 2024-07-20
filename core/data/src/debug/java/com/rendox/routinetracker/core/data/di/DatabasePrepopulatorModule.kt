package com.rendox.routinetracker.core.data.di

import com.rendox.routinetracker.core.data.DatabasePrepopulator
import com.rendox.routinetracker.core.data.DatabasePrepopulatorNoOp
import org.koin.dsl.module

val databasePrepopulatorModule = module {
    single<DatabasePrepopulator> {
        DatabasePrepopulatorNoOp()
    }
}