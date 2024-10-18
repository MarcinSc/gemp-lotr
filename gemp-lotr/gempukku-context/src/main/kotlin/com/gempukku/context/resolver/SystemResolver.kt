package com.gempukku.context.resolver

/**
 * Resolves systems(s) of a particular type from the provided systems.
 */
interface SystemResolver {
    fun <T> resolveValues(systems: List<Any>, clazz: Class<out T>): List<T>
}