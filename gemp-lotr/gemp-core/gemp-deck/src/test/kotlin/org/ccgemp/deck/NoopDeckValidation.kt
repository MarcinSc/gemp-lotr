package org.ccgemp.deck

import com.gempukku.context.resolver.expose.Exposes

@Exposes(DeckValidation::class)
class NoopDeckValidation : DeckValidation {
    override fun getDeckValidator(format: String): DeckValidator {
        return object : DeckValidator {
            override fun isValid(deck: GameDeck?): Boolean {
                return true
            }
        }
    }
}
