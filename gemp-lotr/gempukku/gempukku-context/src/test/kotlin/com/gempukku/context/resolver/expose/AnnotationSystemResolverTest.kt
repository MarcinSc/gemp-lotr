package com.gempukku.context.resolver.expose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AnnotationSystemResolverTest {
    @Test
    fun testResolving() {
        val resolvingSystem = ResolvingSystem()
        val resolver = AnnotationSystemResolver(listOf(resolvingSystem))
        val resolvedSystems = resolver.getSystemsOfType(ExposedInterface::class.java)
        assertEquals(resolvedSystems, listOf(resolvingSystem))
    }

    @Test
    fun testResolvingMultiple() {
        val resolvingSystem1 = ResolvingSystem()
        val resolvingSystem2 = ResolvingSystem()
        val resolver = AnnotationSystemResolver(listOf(resolvingSystem1, resolvingSystem2))
        val resolvedSystems =
            resolver.getSystemsOfType(ExposedInterface::class.java)
        assertEquals(resolvedSystems, listOf(resolvingSystem1, resolvingSystem2))
    }
}
