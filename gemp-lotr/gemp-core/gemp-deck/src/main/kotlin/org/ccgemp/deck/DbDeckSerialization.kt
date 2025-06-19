package org.ccgemp.deck

import org.ccgemp.common.GameDeck

interface DbDeckSerialization {
    fun serializeDeck(deck: GameDeck): String

    fun deserializeDeck(
        name: String,
        notes: String,
        targetFormat: String,
        deck: String,
    ): GameDeck
}
