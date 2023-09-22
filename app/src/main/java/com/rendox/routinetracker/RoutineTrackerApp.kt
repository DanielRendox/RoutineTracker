package com.rendox.routinetracker

import android.app.Application
import com.rendox.routinetracker.routine.di.routineDataModule
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