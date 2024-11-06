package org.ccgemp.tournament.composite

import org.ccgemp.deck.GameDeck
import org.ccgemp.tournament.TournamentParticipant

interface RegisterDeckTournamentProcess : TournamentProcess {
    fun canRegisterDeck(players: List<TournamentParticipant>, player: String, deck: GameDeck): Boolean
}
