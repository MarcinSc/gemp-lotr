package com.gempukku.context.initializer.inject.property

interface PropertyResolver {
    fun resolveProperty(name: String, default: String? = null): String?

    fun getAllPropertyNames(): Set<String>
}
