package com.gempukku.context.processor.inject.property

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*

class PropertiesDeserializer : JsonDeserializer<Properties>() {
    private val pathStack: MutableList<String> = mutableListOf()

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Properties {
        val result = Properties()
        while (true) {
            when (parser.nextToken()) {
                JsonToken.FIELD_NAME -> {
                    pathStack.add(parser.text)
                }

                JsonToken.VALUE_STRING,
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.VALUE_NUMBER_FLOAT,
                JsonToken.VALUE_TRUE,
                JsonToken.VALUE_FALSE -> {
                    result.setProperty(getPath(), parser.valueAsString)
                    pathStack.removeLast()
                }

                JsonToken.END_OBJECT -> {
                    if (pathStack.isNotEmpty())
                        pathStack.removeLast()
                }

                null -> break
                else -> {

                }
            }
        }
        return result
    }

    private fun getPath(): String {
        val result = StringBuilder()
        pathStack.forEach {
            if (result.isNotEmpty())
                result.append(".")
            result.append(it)
        }
        return result.toString()
    }
}