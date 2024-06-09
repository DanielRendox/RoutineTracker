package com.rendox.routinetracker.app

import android.app.Application
import com.rendox.routinetracker.core.data.di.completionHistoryDataModule
import com.rendox.routinetracker.core.data.di.completionTimeDataModule
import com.rendox.routinetracker.core.data.di.routineDataModule
import com.rendox.routinetracker.core.data.di.streakDataModule
import com.rendox.routinetracker.core.data.di.vacationDataModule
import com.rendox.routinetracker.core.database.di.completionTimeLocalDataModule
import com.rendox.routinetracker.core.database.di.habitLocalDataModule
import com.rendox.routinetracker.core.database.di.localDataSourceModule
import com.rendox.routinetracker.core.database.di.streakLocalDataModule
import com.rendox.routinetracker.core.domain.di.completionHistoryDomainModule
import com.rendox.routinetracker.core.domain.di.completionTimeDomainModule
import com.rendox.routinetracker.core.domain.di.domainModule
import com.rendox.routinetracker.core.domain.di.habitDomainModule
import com.rendox.routinetracker.core.domain.di.streakDomainModule
import com.rendox.routinetracker.feature.agenda.di.agendaScreenModule
import com.rendox.routinetracker.routine_details.di.routineDetailsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RoutineTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoutineTrackerApp)
            modules(
                com.rendox.routinetracker.core.database.di.localDataSourceModule,
                com.rendox.routinetracker.core.database.di.habitLocalDataModule,
                com.rendox.routinetracker.core.database.di.completionTimeLocalDataModule,
                streakLocalDataModule,

                routineDataModule,
                completionHistoryDataModule,
                completionTimeDataModule,
                vacationDataModule,
                streakDataModule,

                domainModule,
                habitDomainModule,
                completionHistoryDomainModule,
                streakDomainModule,
                completionTimeDomainModule,

                agendaScreenModule,
                routineDetailsModule,
            )
        }
    }
}