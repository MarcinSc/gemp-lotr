package org.ccgemp.deck

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.common.GameDeck

@Exposes(DeckInterface::class)
class DeckSystem : DeckInterface {
    @Inject
    private lateinit var dbDeckSerialization: DbDeckSerialization

    @Inject
    private lateinit var repository: DeckRepository

    override fun getPlayerDecks(player: String): List<GameDeck> {
        return repository.getPlayerDecks(player).map {
            dbDeckSerialization.deserializeDeck(it.name!!, it.notes!!, it.target_format!!, it.contents!!)
        }
    }

    override fun findDeck(player: String, deckName: String): GameDeck? {
        return repository.findDeck(player, deckName)?.let {
            dbDeckSerialization.deserializeDeck(it.name!!, it.notes!!, it.target_format!!, it.contents!!)
        }
    }

    override fun saveDeck(player: String, deck: GameDeck) {
        val contents = dbDeckSerialization.serializeDeck(deck)
        repository.upsertDeck(player, deck.name, deck.notes, deck.targetFormat, contents)
    }

    override fun renameDeck(player: String, oldDeckName: String, newDeckName: String): Boolean {
        return repository.renameDeck(player, oldDeckName, newDeckName)
    }

    override fun deleteDeck(player: String, deckName: String) {
        repository.deleteDeck(player, deckName)
    }
}
