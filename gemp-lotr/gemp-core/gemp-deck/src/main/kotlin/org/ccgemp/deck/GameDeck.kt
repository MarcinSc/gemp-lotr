package org.ccgemp.deck

data class GameDeck(
    val name: String,
    val notes: String,
    val targetFormat: String,
    val deckParts: Map<String, List<String>>,
)
