package org.ccgemp.deck

interface DeckInterface {
    fun getPlayerDecks(player: String): List<GameDeck>

    fun findDeck(player: String, deckName: String): GameDeck?

    fun saveDeck(player: String, deck: GameDeck)

    fun renameDeck(player: String, oldDeckName: String, newDeckName: String): Boolean

    fun deleteDeck(player: String, deckName: String)

    fun getValidator(format: String): DeckValidator
}
