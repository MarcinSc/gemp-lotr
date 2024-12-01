package org.ccgemp.tournament.composite.pairing

import com.google.common.math.IntMath
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.composite.standing.PlayerStanding
import org.ccgemp.tournament.composite.standing.Standings
import kotlin.math.ceil
import kotlin.math.log2

class DoubleEliminationPairing(
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
        // Check if it's the first double-elem pairing
        if (round == afterRound - 1) {
            val byes = mutableSetOf<String>()
            val pairings = mutableSetOf<Pair<String, String>>()

            pairBracket(standings, notDroppedPlayers, pairings, byes)
            return RoundPairing(pairings, byes)
        } else {
            val byes = mutableSetOf<String>()
            val pairings = mutableSetOf<Pair<String, String>>()

            val lastRoundWinners = matches.filter { it.round == round - 1 }.map { it.winner!! }.filter { it in notDroppedPlayers }

            val lastRoundLosers = matches.filter { it.round == round - 1 && !it.bye }.map { it.loser!! }.filter { it in notDroppedPlayers }
            // Winner bracket
            pairBracket(standings, lastRoundWinners, pairings, byes)
            // Loser bracket
            pairBracket(standings, lastRoundLosers, pairings, byes)

            return RoundPairing(pairings, byes)
        }
    }

    override fun shouldDropLoser(round: Int, player: String, players: List<TournamentParticipant>, matches: List<TournamentMatch>): Boolean {
        return matches.filter { it.round > afterRound && it.loser == player }.size > 1
    }
}