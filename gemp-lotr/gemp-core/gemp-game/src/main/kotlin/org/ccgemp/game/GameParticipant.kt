package org.ccgemp.game

import org.ccgemp.deck.GameDeck

data class GameParticipant(
    val playerId: String,
    val playerDeck: GameDeck,
)
