package org.ccgemp.json

open class TypedRegistry<Config, Type> {
    private val registry: MutableMap<String, (JsonWithConfig<Config>.() -> Type)> = mutableMapOf()

    fun register(type: String, provider: (JsonWithConfig<Config>.() -> Type)) {
        registry[type.lowercase()] = provider
    }

    fun create(value: JsonWithConfig<Config>): Type {
        return registry[value.json.getString("type", null)]!!.invoke(value)
    }
}
