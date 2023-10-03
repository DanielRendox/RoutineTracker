package com.rendox.routinetracker.app

import android.app.Application
import com.rendox.routinetracker.core.data.completion_history.completionHistoryModule
import com.rendox.routinetracker.core.data.routine.routineDataModule
import com.rendox.routinetracker.core.database.dataModule
import com.rendox.routinetracker.feature.routinedetails.routineViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RoutineTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoutineTrackerApp)
            modules(
                dataModule,
                routineDataModule,
                routineViewModelModule,
                completionHistoryModule,
            )
        }
    }
}