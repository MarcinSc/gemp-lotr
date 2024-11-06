package org.ccgemp.tournament

import org.ccgemp.deck.GameDeck

class TournamentParticipant(
    val player: String,
    val decks: MutableList<GameDeck?>,
    var dropped: Boolean = false,
)
