package org.ccgemp.lotr.deck

import com.gempukku.lotro.logic.vo.LotroDeck
import org.ccgemp.common.GameDeck
import org.ccgemp.common.GameDeckItem

const val RING_PART = "ring"
const val RING_BEARER_PART = "rindBearer"
const val MAP_PART = "map"
const val SITES_PART = "sites"
const val DECK_PART = "deck"

fun GameDeck.toLotroDeck(): LotroDeck {
    val result = LotroDeck(name)
    result.notes = notes
    result.ring = deckParts[RING_PART]?.firstOrNull()?.card
    result.ringBearer = deckParts[RING_BEARER_PART]?.firstOrNull()?.card
    result.map = deckParts[MAP_PART]?.firstOrNull()?.card
    deckParts[SITES_PART]?.forEach {
        result.addSite(it.card)
    }
    deckParts[DECK_PART]?.forEach { card ->
        (1..card.count).forEach { _ ->
            result.addCard(card.card)
        }
    }
    return result
}

fun LotroDeck.toGameDeck(): GameDeck {
    val deckParts = mutableMapOf<String, List<GameDeckItem>>()
    ring?.let {
        deckParts.put(RING_PART, listOf(GameDeckItem(it, 1)))
    }
    ringBearer?.let {
        deckParts.put(RING_BEARER_PART, listOf(GameDeckItem(it, 1)))
    }
    map?.let {
        deckParts.put(MAP_PART, listOf(GameDeckItem(it, 1)))
    }
    sites?.let {
        deckParts.put(SITES_PART, it.map { GameDeckItem(it, 1) })
    }
    drawDeckCards?.let {
        deckParts.put(DECK_PART, it.groupingBy { it }.eachCount().map { GameDeckItem(it.key, it.value) })
    }

    return GameDeck(deckName, notes, targetFormat, deckParts)
}
