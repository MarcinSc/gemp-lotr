package org.ccgemp.tournament

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.deck.DeckInterface
import org.ccgemp.deck.DeckValidator
import org.ccgemp.deck.GameDeck

@Exposes(DeckInterface::class)
class DummyDeckSystem : DeckInterface {
    private val playerDecks: MutableMap<String, MutableList<GameDeck>> = mutableMapOf()

    override fun getPlayerDecks(player: String): List<GameDeck> {
        return playerDecks[player].orEmpty()
    }

    override fun findDeck(player: String, deckName: String): GameDeck? {
        return playerDecks[player]?.firstOrNull { it.name == deckName }
    }

    override fun addDeck(player: String, deck: GameDeck): Boolean {
        if (findDeck(player, deck.name) != null) {
            return false
        }
        playerDecks.compute(player) { key, value ->
            value?.let {
                it.add(deck)
                it
            } ?: mutableListOf(deck)
        }
        return true
    }

    override fun getValidator(format: String): DeckValidator {
        return object : DeckValidator {
            override fun isValid(deck: GameDeck?): Boolean {
                return true
            }
        }
    }
}
