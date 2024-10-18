package com.gempukku.context.processor.inject.decorator

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.InjectionException
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WorkerThreadTest {
    @Test
    fun testWorkerThreadOffloading() {
        val proxySystem = ProxySystem()
        val callingSystem = CallingSystem()

        val context = DefaultGempukkuContext(
            null,
            AnnotationSystemResolver(), AnnotationSystemInjector(null, WorkerThreadExecutorSystem()),
            proxySystem, callingSystem
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

        val workerExecutorSystem = WorkerThreadExecutorSystem()
        val context = DefaultGempukkuContext(
            null,
            AnnotationSystemResolver(), AnnotationSystemInjector(null, workerExecutorSystem),
            proxySystem, callingSystem
        )

        context.initialize()

        // This will be executed in the work Thread
        workerExecutorSystem.executorService.submit {
            callingSystem.execute()
            assertTrue(proxySystem.executed)

            assertEquals("Result", callingSystem.executeWithResult())

            assertEquals("Result", callingSystem.executeWithFuture().get())
        }.get()
    }
}