package org.ccgemp.tournament.composite.matches.standing

import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentPlayer

interface Standings {
    fun createStandings(round: Int, players: List<TournamentParticipant>, matches: List<TournamentMatch>): List<PlayerStanding>
}
