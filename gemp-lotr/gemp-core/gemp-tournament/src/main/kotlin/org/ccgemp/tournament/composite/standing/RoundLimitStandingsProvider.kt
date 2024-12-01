package org.ccgemp.tournament.composite.standing

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import kotlin.math.min

@Exposes(LifecycleObserver::class)
class RoundLimitStandingsProvider : LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentStandingsRegistry

    override fun afterContextStartup() {
        val standingsProvider: (JsonWithConfig<StandingsConfig>) -> Standings = {
            RoundLimitStandings(
                registry.create(JsonWithConfig(it.json.get("standings").asObject(), StandingsConfig(it.config.tournamentId))),
                it.json.getInt("round", 0),
            )
        }

        registry.register("roundLimit", standingsProvider)
    }
}

private class RoundLimitStandings(
    private val standings: Standings,
    private val maxRound: Int,
) : Standings {
    override fun createStandings(round: Int, players: List<TournamentParticipant>, matches: List<TournamentMatch>): List<PlayerStanding> {
        return standings.createStandings(min(round, maxRound), players, matches)
    }
}