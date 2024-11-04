package org.ccgemp.json

import org.hjson.JsonObject

class JsonWithConfig<Config>(
    val json: JsonObject,
    val config: Config,
)
