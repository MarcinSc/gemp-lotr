package org.ccgemp.tournament.composite.matches.pairing

import org.ccgemp.json.JsonWithConfig

interface TournamentPairingRegistry {
    fun register(type: String, provider: (JsonWithConfig<PairingConfig>.() -> Pairing))

    fun create(value: JsonWithConfig<PairingConfig>): Pairing
}
