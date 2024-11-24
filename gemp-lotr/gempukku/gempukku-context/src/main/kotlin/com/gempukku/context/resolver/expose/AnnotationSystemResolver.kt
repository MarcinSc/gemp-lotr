package com.gempukku.context.resolver.expose

import com.gempukku.context.resolver.SystemResolver

class AnnotationSystemResolver(
    override val allSystems: Collection<Any>,
) : SystemResolver {
    override fun <T> getSystemsOfType(clazz: Class<out T>): List<T> {
        @Suppress("UNCHECKED_CAST")
        return allSystems.mapNotNull { system ->
            system
                .takeIf {
                    clazz.isAssignableFrom(it.javaClass)
                }?.takeIf {
                    val exposedInterfaces = mutableSetOf<Class<out Any>>()
                    var classToInspect: Class<Any>? = system.javaClass
                    do {
                        exposedInterfaces.addAll(classToInspect!!.getAnnotation(Exposes::class.java)?.value?.map { clazz -> clazz.java }.orEmpty())
                        classToInspect = classToInspect.superclass
                    } while (classToInspect != null)
                    exposedInterfaces.contains(clazz as Any)
                } as T?
        }
    }
}
