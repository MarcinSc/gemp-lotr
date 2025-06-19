package org.ccgemp.lotr.format

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.game.LotroFormat
import org.ccgemp.common.DeckValidator
import org.ccgemp.common.GameDeck
import org.ccgemp.format.GempFormats
import org.ccgemp.lotr.LegacyObjectsProvider
import org.ccgemp.lotr.deck.toLotroDeck

@Exposes(GempFormats::class)
class LotrFormats : GempFormats<LotroFormat> {
    @Inject
    private lateinit var objectsProvider: LegacyObjectsProvider

    override fun findFormat(format: String): LotroFormat? {
        return objectsProvider.formatLibrary.getFormat(format)
    }

    override fun getAllFormats(): List<LotroFormat> {
        return objectsProvider.formatLibrary.allFormats.values.toList()
    }

    override fun getValidator(format: String): DeckValidator {
        val lotroFormat = objectsProvider.formatLibrary.getFormat(format)
        return object : DeckValidator {
            override fun isValid(deck: GameDeck): Boolean {
                return lotroFormat.validateDeck(deck.toLotroDeck()).isEmpty()
            }

            override fun getValidationErrors(deck: GameDeck): List<String> {
                return lotroFormat.validateDeck(deck.toLotroDeck())
            }
        }
    }
}
