package org.ccgemp.deck

interface DeckValidator {
    fun isValid(deck: GameDeck?): Boolean
}
