package com.gempukku.context

import com.gempukku.context.processor.SystemProcessor
import com.gempukku.context.resolver.SystemResolver

class DefaultGempukkuContext(
    override val parent: GempukkuContext? = null,
    private val systemResolver: SystemResolver,
    private val systemProcessor: SystemProcessor,
    private vararg val systems: Any,
) : GempukkuContext {
    override fun <T> getSystems(clazz: Class<T>): List<T> = systemResolver.resolveValues(systems.asList(), clazz)

    fun initialize(): GempukkuContext {
        systemProcessor.processSystems(this, *systems)
        return this
    }
}
