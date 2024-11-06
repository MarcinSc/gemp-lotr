package org.ccgemp.lotr

import com.gempukku.lotro.logic.vo.LotroDeck
import org.ccgemp.deck.GameDeck

const val RING_PART = "ring"
const val RING_BEARER_PART = "rindBearer"
const val MAP_PART = "map"
const val SITES_PART = "sites"
const val DECK_PART = "deck"

fun GameDeck.toLotroDeck(): LotroDeck {
    val result = LotroDeck(name)
    result.notes = notes
    result.ring = deckParts[RING_PART]?.firstOrNull()
    result.ringBearer = deckParts[RING_BEARER_PART]?.firstOrNull()
    result.map = deckParts[MAP_PART]?.firstOrNull()
    deckParts[SITES_PART]?.forEach {
        result.addSite(it)
    }
    deckParts[DECK_PART]?.forEach {
        result.addCard(it)
    }
    return result
}

fun LotroDeck.toGameDeck(): GameDeck {
    val deckParts = mutableMapOf<String, List<String>>()
    ring?.let {
        deckParts.put(RING_PART, listOf(it))
    }
    ringBearer?.let {
        deckParts.put(RING_BEARER_PART, listOf(it))
    }
    map?.let {
        deckParts.put(MAP_PART, listOf(it))
    }
    sites?.let {
        deckParts.put(SITES_PART, it)
    }
    drawDeckCards?.let {
        deckParts.put(DECK_PART, it)
    }

    return GameDeck(deckName, notes, deckParts)
}