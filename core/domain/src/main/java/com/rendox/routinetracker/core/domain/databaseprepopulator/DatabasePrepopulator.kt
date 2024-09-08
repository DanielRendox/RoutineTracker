package com.rendox.routinetracker.core.domain.databaseprepopulator

interface DatabasePrepopulator {
    suspend fun prepopulateDatabase(numOfHabits: Int = 50)
}