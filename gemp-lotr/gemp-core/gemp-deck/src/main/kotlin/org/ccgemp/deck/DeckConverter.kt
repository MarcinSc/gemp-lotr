package org.ccgemp.deck

import org.ccgemp.common.GameDeck
import org.ccgemp.common.mergeTexts
import org.ccgemp.common.splitText

const val DECKS_JOIN = '\n'
const val DECK_VALUES_JOIN = ','
const val DECK_PARTS_JOIN = '\n'
const val DECK_PART_VALUES_JOIN = '='
const val CARD_JOIN = ','

fun String.toMultipleDecks(): List<GameDeck?> {
    return this.splitText(DECKS_JOIN).map { it.toDeck() }
}

fun String.toDeck(): GameDeck? {
    if (this == "") {
        return null
    }
    val deckValues = this.splitText(DECK_VALUES_JOIN)
    return GameDeck(deckValues[0], deckValues[1], deckValues[2], toDeckParts(deckValues[3]))
}

fun List<GameDeck?>.toDecksString(): String {
    return this.map { it.toDeckString() }.mergeTexts(DECKS_JOIN)
}

fun GameDeck?.toDeckString(): String {
    if (this == null) {
        return ""
    }
    return listOf(name, notes, targetFormat, deckPartsToString(deckParts)).mergeTexts(DECK_VALUES_JOIN)
}

fun toDeckParts(text: String): Map<String, List<String>> {
    val parts = text.splitText(DECK_PARTS_JOIN)
    val result = mutableMapOf<String, List<String>>()
    parts.forEach {
        val partSplit = it.splitText(DECK_PART_VALUES_JOIN)
        result[partSplit[0]] = partSplit[1].splitText(CARD_JOIN)
    }
    return result
}

fun deckPartsToString(deckParts: Map<String, List<String>>): String {
    val values =
        deckParts.map {
            listOf(it.key, it.value.mergeTexts(CARD_JOIN)).mergeTexts(DECK_PART_VALUES_JOIN)
        }
    return values.mergeTexts(DECK_PARTS_JOIN)
}
