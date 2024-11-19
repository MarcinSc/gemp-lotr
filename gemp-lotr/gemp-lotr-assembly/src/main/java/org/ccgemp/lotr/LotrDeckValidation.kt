package org.ccgemp.lotr

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.deck.DeckValidation
import org.ccgemp.deck.DeckValidator
import org.ccgemp.deck.GameDeck

@Exposes(DeckValidation::class)
class LotrDeckValidation : DeckValidation {
    @Inject
    private lateinit var objectsProvider: LegacyObjectsProvider

    override fun getDeckValidator(format: String): DeckValidator {
        val lotroFormat = objectsProvider.formatLibrary.getFormat(format)
        return object : DeckValidator {
            override fun isValid(deck: GameDeck?): Boolean {
                return deck != null && lotroFormat.validateDeck(deck.toLotroDeck()).isEmpty()
            }
        }
    }
}
