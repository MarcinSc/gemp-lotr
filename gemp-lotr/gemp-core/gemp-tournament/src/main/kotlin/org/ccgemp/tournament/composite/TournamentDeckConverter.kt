package org.ccgemp.tournament.composite

import org.ccgemp.game.GameDeck

fun getDeck(deck: String, deckIndex: Int): GameDeck {
    return convertToDeck(split(deck, '\n')[deckIndex])
}

fun convertToDeck(text: String): GameDeck {
    val deckValues = split(text)
    return GameDeck(deckValues[0], deckValues[1], toDeckParts(deckValues[2]))
}

fun toDeckParts(text: String): Map<String, List<String>> {
    val parts = split(text, '\n')
    val result = mutableMapOf<String, List<String>>()
    parts.forEach {
        val partSplit = it.split(delimiters = arrayOf("="), limit = 2)
        result[partSplit[0]] = partSplit[1].split(delimiters = arrayOf(","))
    }
    return result
}
