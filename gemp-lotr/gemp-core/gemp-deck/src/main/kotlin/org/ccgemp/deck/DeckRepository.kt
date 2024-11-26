package org.ccgemp.deck

interface DeckRepository {
    fun findDeck(player: String, name: String): GameDeckInfo?

    fun getPlayerDecks(player: String): List<GameDeckInfo>

    fun upsertDeck(
        player: String,
        name: String,
        notes: String,
        targetFormat: String,
        contents: String,
    )

    fun renameDeck(player: String, oldDeckName: String, newDeckName: String): Boolean

    fun deleteDeck(player: String, deckName: String)
}
