package com.gempukku.context.update

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PeriodicUpdateTest {
    @Test
    fun testPeriodicUpdate() {
        val lifecycleSystem = LifecycleSystem()
        val periodicallyUpdatedTestSystem = PeriodicallyUpdatedTestSystem()

        val propertyResolver = PeriodicUpdateTest::class.java.getResourceAsStream("/periodic-update-test.yaml")!!.use {
            YamlPropertyResolver(it)
        }

        val context = DefaultGempukkuContext(
            null, AnnotationSystemResolver(), AnnotationSystemInjector(propertyResolver),
            UpdatingSystem(), WorkerThreadExecutorSystem(), lifecycleSystem, periodicallyUpdatedTestSystem
        )
        context.initialize()

        lifecycleSystem.start()

        Thread.sleep(2500)
        assertEquals(2, periodicallyUpdatedTestSystem.invocationCount)
    }
}