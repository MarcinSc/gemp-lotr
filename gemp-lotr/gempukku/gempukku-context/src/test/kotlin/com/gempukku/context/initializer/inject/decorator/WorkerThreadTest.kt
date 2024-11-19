package com.gempukku.context.initializer.inject.decorator

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.decorator.SimpleThreadPoolFactory
import com.gempukku.context.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.initializer.inject.AnnotationSystemInitializer
import com.gempukku.context.initializer.inject.InjectionException
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class WorkerThreadTest {
    @Test
    fun testWorkerThreadOffloading() {
        val proxySystem = ProxySystem()
        val callingSystem = CallingSystem()

        val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
        val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

        val workerExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)
        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    listOf(
                        proxySystem,
                        callingSystem,
                    ),
                ),
                AnnotationSystemInitializer(null),
                workerExecutorSystem,
            )

        context.initialize()

        callingSystem.execute()
        Thread.sleep(100)
        assertTrue(proxySystem.executed)

        assertThrows(InjectionException::class.java) {
            callingSystem.executeWithResult()
        }

        assertEquals("Result", callingSystem.executeWithFuture().get())
    }

    @Test
    fun testWorkerThreadNoOffloading() {
        val proxySystem = ProxySystem()
        val callingSystem = CallingSystem()

        val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
        val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

        val workerExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)
        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    listOf(
                        proxySystem,
                        callingSystem,
                    ),
                ),
                AnnotationSystemInitializer(null),
                workerExecutorSystem,
            )

        context.initialize()

        // This will be executed in the work Thread
        executorService
            .submit {
                callingSystem.execute()
                assertTrue(proxySystem.executed)

                assertEquals("Result", callingSystem.executeWithResult())

                assertEquals("Result", callingSystem.executeWithFuture().get())
            }.get()
    }
}
