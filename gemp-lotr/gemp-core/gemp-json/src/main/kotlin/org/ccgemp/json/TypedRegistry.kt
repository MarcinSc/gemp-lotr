package org.ccgemp.json

import org.hjson.JsonObject

open class TypedRegistry<Type> {
    private val registry: MutableMap<String, (JsonObject.() -> Type)> = mutableMapOf()

    fun register(type: String, provider: (JsonObject.() -> Type)) {
        registry[type.lowercase()] = provider
    }

    fun create(value: JsonObject): Type {
        return registry[value.getString("type", null)]!!.invoke(value)
    }
}
