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

    override fun getPlayerDecks(player: String): List<GameDeck> {
        return repository.getPlayerDecks(player).map {
            deckSerialization.deserializeDeck(it.name!!, it.notes!!, it.target_format!!, it.contents!!)
        }
    }

    override fun findDeck(player: String, deckName: String): GameDeck? {
        return repository.findDeck(player, deckName)?.let {
            deckSerialization.deserializeDeck(it.name!!, it.notes!!, it.target_format!!, it.contents!!)
        }
    }

    override fun addDeck(player: String, deck: GameDeck): Boolean {
        val deckInfo = repository.findDeck(player, deck.name)
        if (deckInfo != null) {
            return false
        }
        val contents = deckSerialization.serializeDeck(deck)
        repository.createDeck(player, deck.name, deck.notes, deck.targetFormat, contents)
        return true
    }

    override fun getValidator(format: String): DeckValidator {
        return deckValidation.getDeckValidator(format)
    }
}
