package com.gempukku.context.resolver.expose

import org.junit.Assert.assertEquals
import org.junit.Test

internal class AnnotationSystemResolverTest {
    @Test
    fun testResolving() {
        val resolver = AnnotationSystemResolver()
        val resolvingSystem = ResolvingSystem()
        val resolvedSystems = resolver.resolveValues(listOf(resolvingSystem), ExposedInterface::class.java)
        assertEquals(resolvedSystems, listOf(resolvingSystem))
    }

    @Test
    fun testResolvingMultiple() {
        val resolver = AnnotationSystemResolver()
        val resolvingSystem1 = ResolvingSystem()
        val resolvingSystem2 = ResolvingSystem()
        val resolvedSystems =
            resolver.resolveValues(listOf(resolvingSystem1, resolvingSystem2), ExposedInterface::class.java)
        assertEquals(resolvedSystems, listOf(resolvingSystem1, resolvingSystem2))
    }
}
