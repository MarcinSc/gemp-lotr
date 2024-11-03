package org.ccgemp.tournament.composite.matches.pairing

import org.hjson.JsonObject

interface TournamentPairingRegistry {
    fun register(type: String, provider: (JsonObject.() -> Pairing))

    fun create(value: JsonObject): Pairing
}
