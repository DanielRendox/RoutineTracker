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
import com.rendox.routinetracker.core.domain.databaseprepopulator.DatabasePrepopulator
import com.rendox.routinetracker.core.domain.databaseprepopulator.databasePrepopulatorModule
import com.rendox.routinetracker.core.domain.di.completionHistoryDomainModule
import com.rendox.routinetracker.core.domain.di.completionTimeDomainModule
import com.rendox.routinetracker.core.domain.di.domainModule
import com.rendox.routinetracker.core.domain.di.habitDomainModule
import com.rendox.routinetracker.core.domain.di.streakDomainModule
import com.rendox.routinetracker.feature.agenda.di.agendaScreenModule
import com.rendox.routinetracker.routinedetails.di.routineDetailsModule
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named

class RoutineTrackerApp : Application() {
    private val ioDispatcher by inject<CoroutineContext>(qualifier = named("ioDispatcher"))
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val databasePrepopulator by inject<DatabasePrepopulator>()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoutineTrackerApp)
            modules(
                localDataSourceModule,
                habitLocalDataModule,
                completionTimeLocalDataModule,
                streakLocalDataModule,

                routineDataModule,
                completionHistoryDataModule,
                completionTimeDataModule,
                vacationDataModule,
                streakDataModule,
                databasePrepopulatorModule,

                domainModule,
                habitDomainModule,
                completionHistoryDomainModule,
                streakDomainModule,
                completionTimeDomainModule,

                agendaScreenModule,
                routineDetailsModule,
            )
        }

        applicationScope.launch(ioDispatcher) {
            databasePrepopulator.prepopulateDatabase()
        }
    }
}