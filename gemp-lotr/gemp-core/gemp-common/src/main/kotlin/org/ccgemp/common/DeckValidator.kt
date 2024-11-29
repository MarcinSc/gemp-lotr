package org.ccgemp.common

interface DeckValidator {
    fun isValid(deck: GameDeck): Boolean

    fun getValidationErrors(deck: GameDeck): List<String>
}
