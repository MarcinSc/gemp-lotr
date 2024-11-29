package org.ccgemp.tournament

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.deck.DeckInterface
import org.ccgemp.common.DeckValidator
import org.ccgemp.common.GameDeck

@Exposes(DeckInterface::class)
class DummyDeckSystem : DeckInterface {
    private val playerDecks: MutableMap<String, MutableList<GameDeck>> = mutableMapOf()

    override fun getPlayerDecks(player: String): List<GameDeck> {
        return playerDecks[player].orEmpty()
    }

    override fun findDeck(player: String, deckName: String): GameDeck? {
        return playerDecks[player]?.firstOrNull { it.name == deckName }
    }

    override fun saveDeck(player: String, deck: GameDeck) {
        playerDecks.compute(player) { key, value ->
            value?.let {
                it.add(deck)
                it
            } ?: mutableListOf(deck)
        }
    }

    override fun deleteDeck(player: String, deckName: String) {
        playerDecks.compute(player) { key, value ->
            value?.let {
                it.removeIf { it.name == deckName }
                it
            } ?: mutableListOf()
        }
    }

    override fun renameDeck(player: String, oldDeckName: String, newDeckName: String): Boolean {
        val deckToRename = playerDecks[player]?.firstOrNull { it.name == oldDeckName }
        if (deckToRename == null) {
            return false
        }

        playerDecks[player]!!.remove(deckToRename)
        playerDecks[player]!!.add(GameDeck(newDeckName, deckToRename.notes, deckToRename.targetFormat, deckToRename.deckParts))
        return true
    }
}
