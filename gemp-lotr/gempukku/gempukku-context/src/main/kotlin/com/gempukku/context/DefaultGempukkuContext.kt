package com.gempukku.context

import com.gempukku.context.processor.SystemInitializer
import com.gempukku.context.processor.inject.decorator.SystemDecorator
import com.gempukku.context.resolver.SystemResolver

class DefaultGempukkuContext(
    override val parent: GempukkuContext? = null,
    private val systemResolver: SystemResolver,
    private val systemInitializer: SystemInitializer,
    private val systemDecorator: SystemDecorator? = null,
) : GempukkuContext {
    private val systemsByClass: MutableMap<Class<Any>, List<Any>> = mutableMapOf()
    private val decoratedSystems: MutableMap<Pair<Any, Class<Any>>, Any> = mutableMapOf()

    override fun <T> getSystems(clazz: Class<T>): List<T> {
        return systemsByClass.getOrPut(clazz as Class<Any>) {
            systemResolver.getSystemsOfType(clazz)
        } as List<T>
    }

    override fun <T> decorateSystem(system: T, clazz: Class<T>): T {
        return decoratedSystems.getOrPut(system as Any to clazz as Class<Any>) {
            systemDecorator?.decorate(system as Any, clazz as Class<Any>) ?: system as Any
        } as T
    }

    override fun <T> getDecoratedSystems(clazz: Class<T>): List<T> {
        return getSystems(clazz).map { decorateSystem(it, clazz) }
    }

    fun initialize(): GempukkuContext {
        systemInitializer.processSystems(this, systemResolver.allSystems)
        return this
    }
}
