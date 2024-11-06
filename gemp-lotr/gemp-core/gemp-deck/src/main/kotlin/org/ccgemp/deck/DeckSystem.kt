package org.ccgemp.deck

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes

@Exposes(DeckInterface::class)
class DeckSystem : DeckInterface {
    @Inject
    private lateinit var deckSerialization: DeckSerialization

    @Inject
    private lateinit var deckValidation: DeckValidation

    @Inject
    private lateinit var repository: DeckRepository

    override fun findDeck(player: String, deckName: String): GameDeck? {
        return repository.findDeck(player, deckName)?.let {
            deckSerialization.deserializeDeck(it.name, it.notes, it.targetFormat, it.contents)
        }
    }

    override fun getValidator(format: String): DeckValidator {
        return deckValidation.getDeckValidator(format)
    }
}
