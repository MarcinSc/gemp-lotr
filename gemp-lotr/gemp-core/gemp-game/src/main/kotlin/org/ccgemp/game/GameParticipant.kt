package org.ccgemp.game

import org.ccgemp.common.GameDeck

data class GameParticipant(
    val playerId: String,
    val playerDeck: GameDeck,
)
