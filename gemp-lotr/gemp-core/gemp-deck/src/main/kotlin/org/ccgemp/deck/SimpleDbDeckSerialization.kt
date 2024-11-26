package org.ccgemp.deck

import com.gempukku.context.resolver.expose.Exposes

@Exposes(DbDeckSerialization::class)
class SimpleDbDeckSerialization : DbDeckSerialization {
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
