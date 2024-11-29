package org.ccgemp.tournament.composite.standing

import org.ccgemp.json.JsonWithConfig

interface TournamentStandingsRegistry {
    fun register(type: String, provider: (JsonWithConfig<StandingsConfig>.() -> Standings))

    fun create(value: JsonWithConfig<StandingsConfig>): Standings
}