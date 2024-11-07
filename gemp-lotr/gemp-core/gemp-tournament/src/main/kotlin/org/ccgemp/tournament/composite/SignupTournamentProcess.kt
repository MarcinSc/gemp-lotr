package org.ccgemp.tournament.composite

import org.ccgemp.tournament.TournamentParticipant

interface SignupTournamentProcess : RegisterDeckTournamentProcess {
    fun canJoinTournament(players: List<TournamentParticipant>, player: String): Boolean
}
