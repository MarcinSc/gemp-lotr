package com.gempukku.context.processor.inject.property

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.gempukku.context.resource.FileResourceResolver
import com.gempukku.context.resource.createDefaultFileResourceResolver
import java.io.InputStream
import java.util.Properties

class YamlPropertyResolver(
    vararg resources: String,
    fileResourceResolver: FileResourceResolver = createDefaultFileResourceResolver(),
) : PropertyResolver {
    private val properties: Properties

    init {
        val simpleModule = SimpleModule().addDeserializer(Properties::class.java, PropertiesDeserializer())
        val objectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule()).registerModule(simpleModule)
        val result = Properties()
        resources.forEach { resourcePath ->
            val resource = fileResourceResolver.resolveFileResource(resourcePath)
            resource.createInputStream().use {
                val properties = objectMapper.readValue(it, Properties::class.java)
                properties.forEach { key, value -> result[key] = value }
            }
        }
        properties = result
    }

    override fun resolveProperty(
        name: String,
        default: String?,
    ): String? = properties.getProperty(name, default)

    override fun getAllPropertyNames(): Set<String> = properties.stringPropertyNames()
}
