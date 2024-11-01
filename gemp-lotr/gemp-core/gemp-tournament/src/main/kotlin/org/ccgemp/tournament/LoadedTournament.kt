package org.ccgemp.tournament

import org.ccgemp.game.GameDeck
import org.ccgemp.game.GameSettings

interface LoadedTournament {
    fun getPlayerDeck(playerOne: String, round: Int): GameDeck

    fun getGameSettings(round: Int): GameSettings

    val finished: Boolean
    val handler: TournamentHandler
}
