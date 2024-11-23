package org.ccgemp.deck

interface DeckValidator {
    fun isValid(player: String, deck: GameDeck): Boolean
}
