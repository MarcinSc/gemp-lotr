package org.ccgemp.json

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.resource.FileResource
import org.hjson.JsonObject
import org.hjson.JsonValue
import java.io.InputStreamReader

@Exposes(JsonProvider::class)
class JsonProviderSystem : JsonProvider {
    override fun readJson(text: String): JsonObject {
        return JsonValue.readHjson(text).asObject()
    }

    override fun readJson(fileResource: FileResource): JsonObject {
        fileResource.createInputStream()!!.use {
            return JsonValue.readHjson(InputStreamReader(it)).asObject()
        }
    }
}
