package com.rendox.routinetracker.core.data

class DatabasePrepopulatorNoOp : DatabasePrepopulator {
    override suspend fun prepopulateDatabase() {
        // No-op
    }
}