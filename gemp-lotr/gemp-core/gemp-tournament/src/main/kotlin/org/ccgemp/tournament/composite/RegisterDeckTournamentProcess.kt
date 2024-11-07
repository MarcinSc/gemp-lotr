package org.ccgemp.tournament.composite

import org.ccgemp.deck.GameDeck
import org.ccgemp.tournament.TournamentParticipant

interface RegisterDeckTournamentProcess : TournamentProcess {
    val deckTypes: List<String>

    fun canRegisterDecks(players: List<TournamentParticipant>, player: String, decks: List<GameDeck>): Boolean
}
