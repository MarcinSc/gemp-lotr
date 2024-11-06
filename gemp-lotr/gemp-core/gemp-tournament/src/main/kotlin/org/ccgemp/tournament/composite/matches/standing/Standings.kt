package org.ccgemp.tournament.composite.matches.standing

import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant

interface Standings {
    fun createStandings(round: Int, players: List<TournamentParticipant>, matches: List<TournamentMatch>): List<PlayerStanding>
}
