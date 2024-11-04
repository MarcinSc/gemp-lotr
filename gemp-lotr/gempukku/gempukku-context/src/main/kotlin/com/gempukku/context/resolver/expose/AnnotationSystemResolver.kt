package com.gempukku.context.resolver.expose

import com.gempukku.context.resolver.SystemResolver

class AnnotationSystemResolver : SystemResolver {
    override fun <T> resolveValues(systems: Collection<Any>, clazz: Class<out T>): List<T> {
        @Suppress("UNCHECKED_CAST")
        return systems.mapNotNull { system ->
            system
                .takeIf {
                    clazz.isAssignableFrom(it.javaClass)
                }?.takeIf {
                    val exposedInterfaces =
                        it.javaClass.getAnnotation(Exposes::class.java)?.value?.map { clazz ->
                            clazz.java
                        }
                    exposedInterfaces?.contains(clazz as Any) ?: false
                } as T?
        }
    }
}
