package com.gempukku.context

import com.gempukku.context.processor.SystemProcessor
import com.gempukku.context.resolver.SystemResolver

class DefaultGempukkuContext(
    override val parent: GempukkuContext? = null,
    private val systemResolver: SystemResolver,
    private val systemProcessor: SystemProcessor,
) : GempukkuContext {
    override fun <T> getSystems(clazz: Class<T>): List<T> = systemResolver.getSystemsOfType(clazz)

    fun initialize(): GempukkuContext {
        systemProcessor.processSystems(this, systemResolver.allSystems)
        return this
    }
}
