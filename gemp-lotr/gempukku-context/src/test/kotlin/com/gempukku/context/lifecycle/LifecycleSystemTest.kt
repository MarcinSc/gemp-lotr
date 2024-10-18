package com.gempukku.context.lifecycle

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LifecycleSystemTest {
    @Test
    fun testLifecycle() {
        val lifecycleSystem = LifecycleSystem()
        val lifecycleObserver = LifecycleObserverSystem()

        val context = DefaultGempukkuContext(
            null, AnnotationSystemResolver(), AnnotationSystemInjector(),
            lifecycleSystem, lifecycleObserver
        )

        context.initialize()

        assertEquals(null, lifecycleObserver.state)

        lifecycleSystem.start()
        assertEquals(LifecycleObserverSystem.State.STARTED, lifecycleObserver.state)

        lifecycleSystem.pause()
        assertEquals(LifecycleObserverSystem.State.PAUSED, lifecycleObserver.state)

        lifecycleSystem.resume()
        assertEquals(LifecycleObserverSystem.State.RESUMED, lifecycleObserver.state)

        lifecycleSystem.stop()
        assertEquals(LifecycleObserverSystem.State.STOPPED, lifecycleObserver.state)
    }
}