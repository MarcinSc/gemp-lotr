package org.ccgemp.tournament.composite.pairing

import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.composite.standing.Standings

class SingleEliminationPairing(
    private val standings: Standings,
    private val afterRound: Int,
) : Pairing {
    override fun isReady(round: Int): Boolean {
        return true
    }

    override fun createPairings(
        round: Int,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
    ): RoundPairing? {
        val standings = standings.createStandings(afterRound, players, matches)
        val notDroppedPlayers = players.filter { !it.dropped }.map { it.player }
        if (notDroppedPlayers.size <= 1) {
            return null
        }

        val byes = mutableSetOf<String>()
        val pairings = mutableSetOf<Pair<String, String>>()
        pairBracket(standings, notDroppedPlayers, pairings, byes)
        return RoundPairing(pairings, byes)
    }

    override fun shouldDropLoser(
        round: Int,
        player: String,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
    ): Boolean {
        return true
    }
}
