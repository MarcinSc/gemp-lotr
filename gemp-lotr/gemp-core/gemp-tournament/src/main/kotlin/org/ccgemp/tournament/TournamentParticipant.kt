package org.ccgemp.tournament

import org.ccgemp.common.GameDeck

class TournamentParticipant(
    val player: String,
    val decks: MutableMap<String, GameDeck>,
    var dropped: Boolean = false,
)
