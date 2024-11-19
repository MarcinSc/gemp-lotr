package com.gempukku.context.processor.inject.priority

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrioritySortingTest {
    @Test
    fun testEntireSetupA() {
        val aPrioritySystem = APrioritySystem()
        val bPrioritySystem = BPrioritySystem()

        val testSystem = PriorityTestSystem()

        val propertyResolver = YamlPropertyResolver("classpath:/priority-test-a.yaml")

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    listOf(
                        aPrioritySystem,
                        bPrioritySystem,
                        testSystem,
                    ),
                ),
                AnnotationSystemInjector(propertyResolver),
            )

        context.initialize()

        // Unsorted
        assertEquals(2, testSystem.noDefaultNoPriority.size)
        // Sorted
        assertEquals(listOf(aPrioritySystem, bPrioritySystem), testSystem.noDefaultWithPriority)
        assertEquals(listOf(aPrioritySystem, bPrioritySystem), testSystem.defaultNoPriority)
        assertEquals(listOf(aPrioritySystem, bPrioritySystem), testSystem.defaultWithPriority)
    }

    @Test
    fun testEntireSetupB() {
        val aPrioritySystem = APrioritySystem()
        val bPrioritySystem = BPrioritySystem()

        val testSystem = PriorityTestSystem()

        val propertyResolver = YamlPropertyResolver("classpath:/priority-test-b.yaml")

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    listOf(
                        aPrioritySystem,
                        bPrioritySystem,
                        testSystem,
                    ),
                ),
                AnnotationSystemInjector(propertyResolver),
            )

        context.initialize()

        // Unsorted
        assertEquals(2, testSystem.noDefaultNoPriority.size)
        // Sorted
        assertEquals(listOf(bPrioritySystem, aPrioritySystem), testSystem.noDefaultWithPriority)
        assertEquals(listOf(aPrioritySystem, bPrioritySystem), testSystem.defaultNoPriority)
        assertEquals(listOf(bPrioritySystem, aPrioritySystem), testSystem.defaultWithPriority)
    }
}
