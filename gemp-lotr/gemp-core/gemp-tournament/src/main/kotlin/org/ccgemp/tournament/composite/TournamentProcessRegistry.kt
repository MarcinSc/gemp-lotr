package org.ccgemp.tournament.composite

import org.ccgemp.json.JsonWithConfig

interface TournamentProcessRegistry {
    fun register(type: String, provider: (JsonWithConfig<TournamentProcessConfig>.() -> TournamentProcess))

    fun create(config: JsonWithConfig<TournamentProcessConfig>): TournamentProcess
}
