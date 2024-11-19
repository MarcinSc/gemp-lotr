package com.gempukku.context.resolver

/**
 * Resolves systems(s) of a particular type from the provided systems.
 */
interface SystemResolver {
    val allSystems: Collection<Any>

    fun <T> getSystemsOfType(clazz: Class<out T>): List<T>
}
