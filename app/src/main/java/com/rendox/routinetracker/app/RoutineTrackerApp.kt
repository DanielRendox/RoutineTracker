package com.rendox.routinetracker.app

import android.app.Application
import com.rendox.routinetracker.core.data.routine.dataModule
import com.rendox.routinetracker.core.data.routine.routineDataModule
import com.rendox.routinetracker.feature.routinedetails.routineModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RoutineTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoutineTrackerApp)
            modules(dataModule, routineDataModule, routineModule)
        }
    }
}