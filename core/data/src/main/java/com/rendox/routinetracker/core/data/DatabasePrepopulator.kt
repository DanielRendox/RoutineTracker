package com.rendox.routinetracker.core.data

interface DatabasePrepopulator {
    suspend fun prepopulateDatabase()
}