package com.gempukku.context.processor.inject.property

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream
import java.util.Properties

class YamlPropertyResolver(
    file: InputStream,
) : PropertyResolver {
    private val properties: Properties

    init {
        val simpleModule = SimpleModule().addDeserializer(Properties::class.java, PropertiesDeserializer())
        val objectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule()).registerModule(simpleModule)
        properties = objectMapper.readValue(file, Properties::class.java)
    }

    override fun resolveProperty(
        name: String,
        default: String?,
    ): String? = properties.getProperty(name, default)

    override fun getAllPropertyNames(): Set<String> = properties.stringPropertyNames()
}
