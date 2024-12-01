package org.ccgemp.common

data class GameDeck(
    val name: String,
    val notes: String,
    val targetFormat: String,
    val deckParts: Map<String, List<GameDeckItem>>,
)
