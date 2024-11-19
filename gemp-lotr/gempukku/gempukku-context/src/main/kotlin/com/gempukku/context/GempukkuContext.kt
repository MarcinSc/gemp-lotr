package com.gempukku.context

/**
 * Context is a grouping of systems that interact with each other.
 */
interface GempukkuContext {
    val parent: GempukkuContext?

    fun <T> getSystems(clazz: Class<T>): List<T>

    fun <T> decorateSystem(system: T, clazz: Class<T>): T

    fun <T> getDecoratedSystems(clazz: Class<T>): List<T>
}
