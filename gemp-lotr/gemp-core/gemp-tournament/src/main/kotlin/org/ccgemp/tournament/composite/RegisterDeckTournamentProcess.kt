package org.ccgemp.tournament.composite

import org.ccgemp.common.GameDeck
import org.ccgemp.tournament.TournamentParticipant

interface RegisterDeckTournamentProcess : TournamentProcess {
    val deckTypes: List<String>

    fun canRegisterDecks(
        round: Int,
        stage: String,
        players: List<TournamentParticipant>,
        player: String,
        decks: List<GameDeck>,
    ): Boolean
}
