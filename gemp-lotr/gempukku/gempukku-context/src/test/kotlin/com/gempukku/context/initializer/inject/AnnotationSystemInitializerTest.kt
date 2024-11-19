package com.gempukku.context.initializer.inject

import com.gempukku.context.GempukkuContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever

internal class AnnotationSystemInitializerTest {
    @Test
    fun testInjectSuccess() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem = InjectedSystem()
        whenever(context.getDecoratedSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))

        val injectingSystem = SingleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertEquals(injectingSystem.injectedSystem, injectedSystem)
    }

    @Test
    fun testInjectFromParent() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)

        val injectedSystem = InjectedSystem()
        whenever(context.getDecoratedSystems(InjectedSystem::class.java)).thenReturn(emptyList())
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getDecoratedSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))

        val injectingSystem = FromAncestorsSingleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertEquals(injectingSystem.injectedSystem, injectedSystem)
    }

    @Test
    fun testInjectFailsEventIfParentHas() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)

        val injectedSystem = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))

        val injectingSystem = SingleInjectingSystem()
        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, listOf(injectingSystem))
        }
    }

    @Test
    fun testInjectFromParentFailsNotNull() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)

        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = FromAncestorsSingleInjectingSystem()
        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, listOf(injectingSystem))
        }
    }

    @Test
    fun testInjectFailsNotNull() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = SingleInjectingSystem()

        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, listOf(injectingSystem))
        }
    }

    @Test
    fun testInjectAllowsNull() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = NullableSingleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertNull(injectingSystem.injectedSystem)
    }

    @Test
    fun testInjectFailsTooMany() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(InjectedSystem(), InjectedSystem()))

        val injectingSystem = SingleInjectingSystem()

        assertThrows(InjectionException::class.java) {
            injector.processSystems(context, listOf(injectingSystem))
        }
    }

    @Test
    fun testMultipleInjectsEmpty() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(emptyList())

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertEquals(injectingSystem.injectedSystems, emptyList<InjectedSystem>())
    }

    @Test
    fun testMultipleInjectsOneValue() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem))
        whenever(context.decorateSystem(injectedSystem, InjectedSystem::class.java)).thenReturn(injectedSystem)

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem))
    }

    @Test
    fun testMultipleInjectsMultipleValues() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem1 = InjectedSystem()
        val injectedSystem2 = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem1, injectedSystem2))
        whenever(context.decorateSystem(injectedSystem1, InjectedSystem::class.java)).thenReturn(injectedSystem1)
        whenever(context.decorateSystem(injectedSystem2, InjectedSystem::class.java)).thenReturn(injectedSystem2)

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem1, injectedSystem2))
    }

    @Test
    fun testInjectsWithParentOneValue() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem1 = InjectedSystem()
        val injectedSystem2 = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem1))
        whenever(context.decorateSystem(injectedSystem1, InjectedSystem::class.java)).thenReturn(injectedSystem1)
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem2))
        whenever(parentContext.decorateSystem(injectedSystem2, InjectedSystem::class.java)).thenReturn(injectedSystem2)

        val injectingSystem = MultipleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem1))
    }

    @Test
    fun testInjectsWithParentConcatenatesValues() {
        val injector = AnnotationSystemInitializer()

        val context = Mockito.mock(GempukkuContext::class.java)
        val parentContext = Mockito.mock(GempukkuContext::class.java)
        val injectedSystem1 = InjectedSystem()
        val injectedSystem2 = InjectedSystem()
        whenever(context.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem1))
        whenever(context.decorateSystem(injectedSystem1, InjectedSystem::class.java)).thenReturn(injectedSystem1)
        whenever(context.parent).thenReturn(parentContext)
        whenever(parentContext.getSystems(InjectedSystem::class.java)).thenReturn(listOf(injectedSystem2))
        whenever(parentContext.decorateSystem(injectedSystem2, InjectedSystem::class.java)).thenReturn(injectedSystem2)

        val injectingSystem = FromAncestorsMultipleInjectingSystem()
        injector.processSystems(context, listOf(injectingSystem))

        assertEquals(injectingSystem.injectedSystems, listOf(injectedSystem1, injectedSystem2))
    }
}
