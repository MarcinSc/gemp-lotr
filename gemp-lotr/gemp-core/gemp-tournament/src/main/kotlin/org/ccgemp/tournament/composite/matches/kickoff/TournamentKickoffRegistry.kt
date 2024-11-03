package org.ccgemp.tournament.composite.matches.kickoff

import org.hjson.JsonObject

interface TournamentKickoffRegistry {
    fun register(type: String, provider: (JsonObject.() -> Kickoff))

    fun create(value: JsonObject): Kickoff
}
