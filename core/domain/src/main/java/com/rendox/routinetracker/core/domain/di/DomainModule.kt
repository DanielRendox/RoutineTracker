package com.rendox.routinetracker.core.domain.di

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

val domainModule = module {
    single<CoroutineContext>(named("defaultDispatcher")) {
        Dispatchers.Default
    }
}