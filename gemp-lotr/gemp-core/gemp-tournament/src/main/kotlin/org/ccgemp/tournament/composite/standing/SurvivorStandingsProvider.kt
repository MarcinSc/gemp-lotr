package org.ccgemp.tournament.composite.standing

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant

@Exposes(LifecycleObserver::class)
class SurvivorStandingsProvider : LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentStandingsRegistry

    override fun afterContextStartup() {
        val standingsProvider: (JsonWithConfig<StandingsConfig>) -> Standings = {
            SurvivorStandings(
                registry.create(JsonWithConfig(it.json.get("secondaryStandings").asObject(), StandingsConfig(it.config.tournamentId))),
            )
        }

        registry.register("survivor", standingsProvider)
    }
}

private class SurvivorStandings(
    private val secondaryStandings: Standings,
) : Standings {
    override fun createStandings(round: Int, players: List<TournamentParticipant>, matches: List<TournamentMatch>): List<PlayerStanding> {
        val acceptedMatches = matches.filter { it.round <= round }
        val secondaryStandings = secondaryStandings.createStandings(round, players, matches)

        val result = mutableListOf<PlayerStanding>()
        val placedPlayers = mutableSetOf<String>()
        var standing = 0
        (round downTo 1).forEach { evaluatedRound ->
            val playersPlayedInRound = acceptedMatches.filter { it.round == evaluatedRound }.flatMapTo(mutableSetOf()) { it.players }
            val playersToPlace = playersPlayedInRound - placedPlayers
            var lastStandingInSecondary = -1
            secondaryStandings.filter { it.name in playersToPlace }.forEach {
                if (it.standing == lastStandingInSecondary) {
                    result.add(PlayerStanding(it.name, standing, it.points, it.stats))
                } else {
                    standing = result.size + 1
                    result.add(PlayerStanding(it.name, standing, it.points, it.stats))
                }
                lastStandingInSecondary = it.standing
            }
            placedPlayers.addAll(playersToPlace)
        }
        // Players that did not play any games
        standing = result.size + 1
        val remainingPlayers = players.filter { it.player !in placedPlayers }.map { it.player }
        secondaryStandings.filter { it.name in remainingPlayers }.forEach {
            result.add(PlayerStanding(it.name, standing, it.points, it.stats))
        }
        return result
    }
}
