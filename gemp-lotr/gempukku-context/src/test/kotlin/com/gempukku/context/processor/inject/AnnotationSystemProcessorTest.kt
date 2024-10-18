package com.gempukku.context.processor.inject

import com.gempukku.context.GempukkuContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever

internal class AnnotationSystemProcessorTest {
    @Test
    fun testInjectSuccess() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))

        val injectingSystem = SingleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertEquals(injectingSystem.injectedSystem, injectedSystem)
    }

    @Test
    fun testInjectFromParent() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)

        val injectedSystem = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))

        val injectingSystem = FromAncestorsSingleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertEquals(injectingSystem.injectedSystem, injectedSystem)
    }

    @Test
    fun testInjectFailsEventIfParentHas() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)

        val injectedSystem = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))

        val injectingSystem = SingleInjectingSystem()
        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, injectingSystem)
        }
    }

    @Test
    fun testInjectFromParentFailsNotNull() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)

        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = FromAncestorsSingleInjectingSystem()
        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, injectingSystem)
        }
    }

    @Test
    fun testInjectFailsNotNull() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = SingleInjectingSystem()

        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, injectingSystem)
        }
    }

    @Test
    fun testInjectAllowsNull() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = NullableSingleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertNull(injectingSystem.injectedSystem)
    }

    @Test
    fun testInjectFailsTooMany() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(InjectedSystem(), InjectedSystem()))

        val injectingSystem = SingleInjectingSystem()

        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, injectingSystem)
        }
    }

    @Test
    fun testMultipleInjectsEmpty() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertEquals(injectingSystem.injectedSystems, emptyList<InjectedSystem>())
    }

    @Test
    fun testMultipleInjectsOneValue() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem))
    }

    @Test
    fun testMultipleInjectsMultipleValues() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem1 = InjectedSystem()
        val injectedSystem2 = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem1, injectedSystem2))

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem1, injectedSystem2))
    }

    @Test
    fun testInjectsWithParentOneValue() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem1 = InjectedSystem()
        val injectedSystem2 = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem1))
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem2))

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem1))
    }

    @Test
    fun testInjectsWithParentConcatenatesValues() {
        val injector = AnnotationSystemInjector()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem1 = InjectedSystem()
        val injectedSystem2 = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem1))
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem2))

        val injectingSystem = FromAncestorsMultipleInjectingSystem()
        injector.processSystems(context, injectingSystem)

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem1, injectedSystem2))
    }
}