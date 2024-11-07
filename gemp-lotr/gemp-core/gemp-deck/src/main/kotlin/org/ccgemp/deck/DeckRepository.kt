package org.ccgemp.deck

interface DeckRepository {
    fun findDeck(player: String, name: String): GameDeckInfo?

    fun getPlayerDecks(player: String): List<GameDeckInfo>

    fun createDeck(
        player: String,
        name: String,
        notes: String,
        targetFormat: String,
        contents: String,
    )
}
