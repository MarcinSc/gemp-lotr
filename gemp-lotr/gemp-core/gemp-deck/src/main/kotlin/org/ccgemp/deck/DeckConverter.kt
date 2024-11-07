package org.ccgemp.deck

const val DECKS_JOIN = '\n'
const val DECK_VALUES_JOIN = ','
const val DECK_PARTS_JOIN = '\n'
const val DECK_PART_VALUES_JOIN = '='
const val CARD_JOIN = ','

fun String.toMultipleDecks(): List<GameDeck?> {
    return split(this, DECKS_JOIN).map { it.toDeck() }
}

fun String.toDeck(): GameDeck? {
    if (this == "") {
        return null
    }
    val deckValues = split(this, DECK_VALUES_JOIN)
    return GameDeck(deckValues[0], deckValues[1], deckValues[2], toDeckParts(deckValues[3]))
}

fun List<GameDeck?>.toDecksString(): String {
    return merge(this.map { it.toDeckString() }, DECKS_JOIN)
}

fun GameDeck?.toDeckString(): String {
    if (this == null) {
        return ""
    }
    return merge(listOf(name, notes, targetFormat, toString(deckParts)))
}

private fun toDeckParts(text: String): Map<String, List<String>> {
    val parts = split(text, DECK_PARTS_JOIN)
    val result = mutableMapOf<String, List<String>>()
    parts.forEach {
        val partSplit = split(it, DECK_PART_VALUES_JOIN)
        result[partSplit[0]] = split(partSplit[1], CARD_JOIN)
    }
    return result
}

private fun toString(deckParts: Map<String, List<String>>): String {
    val values =
        deckParts.map {
            merge(listOf(it.key, merge(it.value, CARD_JOIN)), DECK_PART_VALUES_JOIN)
        }
    return merge(values, DECK_PARTS_JOIN)
}
