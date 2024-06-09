package com.rendox.routinetracker.core.domain.di

import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val domainModule = module {
    single<CoroutineContext>(named("defaultDispatcher")) {
        Dispatchers.Default
    }
}