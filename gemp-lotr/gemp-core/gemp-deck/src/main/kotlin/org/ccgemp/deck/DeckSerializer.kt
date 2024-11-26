package org.ccgemp.deck

import org.w3c.dom.Document

interface DeckSerializer {
    fun renderDeckList(decks: List<GameDeck>): Document

    fun renderDeck(deck: GameDeck): Document

    fun deserializeDeck(
        name: String,
        targetFormat: String,
        notes: String,
        contents: String,
    ): GameDeck?
}
