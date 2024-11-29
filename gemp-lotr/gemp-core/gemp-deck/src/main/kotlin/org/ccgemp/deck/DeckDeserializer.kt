package org.ccgemp.deck

import org.ccgemp.common.GameDeck

interface DeckDeserializer {
    fun deserializeDeck(
        name: String,
        targetFormat: String,
        notes: String,
        contents: String,
    ): GameDeck?
}
