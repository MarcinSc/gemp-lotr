package org.ccgemp.deck

interface DeckInterface {
    fun findDeck(player: String, deckName: String): GameDeck?
    fun getValidator(format: String): DeckValidator
}