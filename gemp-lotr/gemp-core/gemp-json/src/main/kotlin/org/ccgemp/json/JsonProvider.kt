package org.ccgemp.json

import com.gempukku.context.resource.FileResource
import org.hjson.JsonObject

interface JsonProvider {
    fun readJson(text: String): JsonObject

    fun readJson(fileResource: FileResource): JsonObject

    fun <T> readJsonObject(text: String, clazz: Class<T>): T

    fun writeJsonObject(obj: Any): String
}
