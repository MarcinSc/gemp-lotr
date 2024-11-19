package com.gempukku.context.update

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.decorator.SimpleThreadPoolFactory
import com.gempukku.context.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.initializer.inject.AnnotationSystemInitializer
import com.gempukku.context.initializer.inject.property.YamlPropertyResolver
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class PeriodicUpdateTest {
    @Test
    fun testPeriodicUpdate() {
        val lifecycleSystem = LifecycleSystem()
        val periodicallyUpdatedTestSystem = PeriodicallyUpdatedTestSystem()

        val propertyResolver = YamlPropertyResolver("classpath:/periodic-update-test.yaml")

        val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
        val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

        val workerExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    listOf(
                        UpdatingSystem(),
                        workerExecutorSystem,
                        lifecycleSystem,
                        periodicallyUpdatedTestSystem,
                    ),
                ),
                AnnotationSystemInitializer(propertyResolver),
            )
        context.initialize()

        lifecycleSystem.start()

        Thread.sleep(2500)
        assertEquals(2, periodicallyUpdatedTestSystem.invocationCount)
    }
}
