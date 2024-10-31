package com.gempukku.context.update

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.decorator.SimpleThreadPoolFactory
import com.gempukku.context.processor.inject.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class PeriodicUpdateTest {
    @Test
    fun testPeriodicUpdate() {
        val lifecycleSystem = LifecycleSystem()
        val periodicallyUpdatedTestSystem = PeriodicallyUpdatedTestSystem()

        val propertyResolver =
            PeriodicUpdateTest::class.java.getResourceAsStream("/periodic-update-test.yaml")!!.use {
                YamlPropertyResolver(it)
            }

        val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
        val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

        val workerExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(),
                AnnotationSystemInjector(propertyResolver),
                UpdatingSystem(),
                workerExecutorSystem,
                lifecycleSystem,
                periodicallyUpdatedTestSystem,
            )
        context.initialize()

        lifecycleSystem.start()

        Thread.sleep(2500)
        assertEquals(2, periodicallyUpdatedTestSystem.invocationCount)
    }
}
