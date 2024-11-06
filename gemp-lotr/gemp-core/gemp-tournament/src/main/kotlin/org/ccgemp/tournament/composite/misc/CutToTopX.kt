package org.ccgemp.tournament.composite.misc

import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.TournamentProcess
import org.ccgemp.tournament.composite.matches.standing.Standings

class CutToTopX(
    private val x: Int,
    private val standings: Standings,
) : TournamentProcess {
    override fun processTournament(
        round: Int,
        stage: String,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
        tournamentProgress: TournamentProgress,
    ) {
        val notDroppedPlayers = players.filter { !it.dropped }.map { it.player }

        val standings = standings.createStandings(round, players, matches)
        val notDroppedPlayersInStandingsOrder = standings.filter { notDroppedPlayers.contains(it.name) }.map { it.name }
        val survivingPlayers = notDroppedPlayersInStandingsOrder.take(x)

        val playersToDrop = notDroppedPlayersInStandingsOrder - survivingPlayers.toSet()

        playersToDrop.forEach {
            tournamentProgress.dropPlayer(it)
        }

        tournamentProgress.updateState(round, FINISHED_STAGE)
    }

    override fun getTournamentStatus(stage: String): String {
        return "Performing cut to top $x"
    }
}
