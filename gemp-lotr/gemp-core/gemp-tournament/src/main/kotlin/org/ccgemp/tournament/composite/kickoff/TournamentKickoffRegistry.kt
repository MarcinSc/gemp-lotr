package org.ccgemp.tournament.composite.kickoff

import org.ccgemp.json.JsonWithConfig

interface TournamentKickoffRegistry {
    fun register(type: String, provider: (JsonWithConfig<KickoffConfig>.() -> Kickoff))

    fun create(value: JsonWithConfig<KickoffConfig>): Kickoff
}
