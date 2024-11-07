package org.ccgemp.deck

import com.gempukku.context.resolver.expose.Exposes

@Exposes(DeckSerialization::class)
class SimpleDeckSerialization : DeckSerialization {
    override fun serializeDeck(deck: GameDeck): String {
        return deckPartsToString(deck.deckParts)
    }

    override fun deserializeDeck(
        name: String,
        notes: String,
        targetFormat: String,
        deck: String,
    ): GameDeck {
        return GameDeck(name, notes, targetFormat, toDeckParts(deck))
    }
}
