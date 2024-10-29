package com.gempukku.context.processor.inject.decorator

import com.gempukku.context.ContextScheduledExecutor
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.InjectionException
import com.gempukku.context.resolver.expose.Exposes
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import java.util.logging.Level
import java.util.logging.Logger

private val log: Logger = Logger.getLogger(AnnotationSystemInjector::class.java.name)

@Exposes(ContextScheduledExecutor::class)
class WorkerThreadExecutorSystem(
    private val threadPool: ThreadPool,
    private val executorService: ScheduledExecutorService,
) : SystemDecorator,
    ContextScheduledExecutor {
    override fun <T> decorate(
        system: T,
        systemClass: Class<T>,
    ): T {
        val methodsThatCanBeOffloadedToWorkerThread = mutableSetOf<Method>()

        systemClass.declaredMethods.forEach { method ->
            if (method.returnType == Void.TYPE || method.returnType == Future::class.java) {
                methodsThatCanBeOffloadedToWorkerThread.add(method)
            }
        }

        val handler =
            InvocationHandler { _, method, args ->
                if (threadPool.containsThread(Thread.currentThread())) {
                    callMethod(system, method, args)
                } else {
                    if (methodsThatCanBeOffloadedToWorkerThread.contains(method)) {
                        if (method.returnType == Void.TYPE) {
                            executorService.execute {
                                callMethod(system, method, args)
                            }
                        } else {
                            val methodCall =
                                Supplier<Future<Any>> {
                                    callMethod(system, method, args) as Future<Any>
                                }
                            CompletableFuture.supplyAsync(methodCall, executorService).thenApply {
                                it.get()
                            }
                        }
                    } else {
                        throw InjectionException(
                            "Unable to offload method ${method.declaringClass.name}::${method.name} to separate Thread, " +
                                "method must return void/Unit or Future object",
                        )
                    }
                }
            }

        return Proxy.newProxyInstance(systemClass.classLoader, arrayOf(systemClass), handler) as T
    }

    override fun submit(runnable: Runnable): Future<*> = executorService.submit(runnable)

    override fun <T> submit(callable: Callable<T>): Future<T> = executorService.submit(callable)

    override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
    ): Runnable {
        val scheduleAtFixedRate = executorService.scheduleAtFixedRate(command, initialDelay, period, unit)
        return Runnable {
            scheduleAtFixedRate.cancel(false)
        }
    }

    private fun <T> callMethod(
        system: T,
        method: Method,
        args: Array<out Any>?,
    ): Any? =
        try {
            if (args == null) {
                method.invoke(system)
            } else {
                method.invoke(system, *args)
            }
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Failed executing in ${Thread.currentThread().name}", e)
        }
}
