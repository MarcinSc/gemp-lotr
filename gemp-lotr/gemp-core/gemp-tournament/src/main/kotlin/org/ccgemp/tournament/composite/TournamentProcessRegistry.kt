package org.ccgemp.tournament.composite

import org.hjson.JsonObject

interface TournamentProcessRegistry {
    fun registerProcess(type: String, provider: (Pair<JsonObject, Int>.() -> TournamentProcess))
}
