package com.rendox.routinetracker.core.logic

import kotlin.system.measureTimeMillis

inline fun <T> measureTimeMillisForResult(operation: () -> T): Pair<T, Long> {
    val result: T
    val duration = measureTimeMillis {
        result = operation()
    }
    return result to duration
}