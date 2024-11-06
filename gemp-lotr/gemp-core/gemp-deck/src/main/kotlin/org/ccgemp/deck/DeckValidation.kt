package org.ccgemp.deck

interface DeckValidation {
    fun getDeckValidator(format: String): DeckValidator
}