package org.ccgemp.deck

interface DeckSerialization {
    fun serializeDeck(deck: GameDeck): String

    fun deserializeDeck(
        name: String,
        notes: String,
        targetFormat: String,
        deck: String,
    ): GameDeck
}
