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

        val propertyResolver = PriorityTestSystem::class.java.getResourceAsStream("/priority-test-a.yaml")!!.use {
            YamlPropertyResolver(it)
        }

        val context = DefaultGempukkuContext(
            null, AnnotationSystemResolver(), AnnotationSystemInjector(propertyResolver),
            aPrioritySystem, bPrioritySystem, testSystem,
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

        val propertyResolver = PriorityTestSystem::class.java.getResourceAsStream("/priority-test-b.yaml")!!.use {
            YamlPropertyResolver(it)
        }

        val context = DefaultGempukkuContext(
            null, AnnotationSystemResolver(), AnnotationSystemInjector(propertyResolver),
            aPrioritySystem, bPrioritySystem, testSystem,
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