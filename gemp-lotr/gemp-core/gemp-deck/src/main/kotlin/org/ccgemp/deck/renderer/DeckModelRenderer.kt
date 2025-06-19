package org.ccgemp.deck.renderer

import com.gempukku.server.ResponseWriter
import org.ccgemp.common.GameDeck

interface DeckModelRenderer {
    fun renderListDecks(player: String, playerDecks: List<GameDeck>, responseWriter: ResponseWriter)

    fun renderGetDeck(player: String, deck: GameDeck, responseWriter: ResponseWriter)
}
