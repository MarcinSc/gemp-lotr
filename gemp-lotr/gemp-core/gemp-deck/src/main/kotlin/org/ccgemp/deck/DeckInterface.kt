package org.ccgemp.deck

interface DeckInterface {
    fun getPlayerDecks(player: String): List<GameDeck>

    fun findDeck(player: String, deckName: String): GameDeck?

    fun addDeck(player: String, deck: GameDeck): Boolean

    fun getValidator(format: String): DeckValidator
}
