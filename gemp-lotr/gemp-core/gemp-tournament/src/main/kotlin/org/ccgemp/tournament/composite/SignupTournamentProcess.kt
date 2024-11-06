package org.ccgemp.tournament.composite

import org.ccgemp.deck.GameDeck
import org.ccgemp.tournament.TournamentParticipant

interface SignupTournamentProcess: TournamentProcess {
    fun canJoinTournament(
        players: List<TournamentParticipant>,
        player: String, decks: List<GameDeck?>): Boolean
}