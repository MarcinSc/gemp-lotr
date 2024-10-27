package com.gempukku.context

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

interface ContextScheduledExecutor {
    fun submit(runnable: Runnable): Future<*>
    fun <T> submit(callable: Callable<T>): Future<T>
    fun scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): Runnable
}